#!/bin/sh
# Enqueues every series on every service, then drains each queue.
# Usage: scrape-all.sh [gateway], where gateway defaults to http://localhost:8081 (Compose); Vite's is :5173.
# Each service runs its own scrape, in parallel, and the calls return when it is done (SCB's first takes ~12 minutes).
# The exception is a 409: that service's cron is already draining its queue, so the script exits 0 with that run still
# going. Check the service's log to see when it has finished.
# The scrape paths need an admin: set MACRO_HUB_ADMIN_EMAIL and MACRO_HUB_ADMIN_PASSWORD (needs jq).
set -u

gateway="${1:-http://localhost:8081}"

if [ -z "${MACRO_HUB_ADMIN_EMAIL:-}" ] || [ -z "${MACRO_HUB_ADMIN_PASSWORD:-}" ]; then
    echo "Set MACRO_HUB_ADMIN_EMAIL and MACRO_HUB_ADMIN_PASSWORD: the scrape paths need an admin account." >&2
    exit 1
fi

# The password goes through stdin, so it isn't in the process list. The token is only checked when a call starts, so a
# scrape that outlasts it isn't cut off
token=$(jq -n --arg email "$MACRO_HUB_ADMIN_EMAIL" --arg password "$MACRO_HUB_ADMIN_PASSWORD" '{email: $email, password: $password}' \
    | curl -s -X POST -H 'Content-Type: application/json' -d @- "$gateway/macro-analyzer/auth/login" \
    | jq -r '.accessToken // empty')
if [ -z "$token" ]; then
    echo "Login to $gateway failed." >&2
    exit 1
fi

services="regional-statistics macro-series parliament election"

status=0
for service in $services; do
    prefix="macro-analyzer/scrape/$service"
    code=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H "Authorization: Bearer $token" "$gateway/$prefix/enqueue-all")
    echo "POST /$prefix/enqueue-all: $code"
    [ "$code" = "202" ] || status=1
done
[ "$status" -eq 0 ] || exit "$status"

results=$(mktemp) || exit 1
trap 'rm -f "$results"' EXIT
for service in $services; do
    prefix="macro-analyzer/scrape/$service"
    (
        code=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H "Authorization: Bearer $token" "$gateway/$prefix/scrape-from-queue")
        # 409: the service's cron is already draining the queue, which is what this call would have done
        echo "POST /$prefix/scrape-from-queue: $code" >> "$results"
    ) &
done
wait
cat "$results"
if grep -Ev ': (200|409)' "$results" >/dev/null; then
    exit 1
fi
