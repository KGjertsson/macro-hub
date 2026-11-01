#!/bin/sh
# Replaces the global POST /macro-analyzer/enqueue-all: enqueues every series on every service, then drains each queue.
# Usage: scrape-all.sh [gateway], where gateway defaults to http://localhost:8081 (Compose); Vite's is :5173.
# Each service runs its own scrape, in parallel, and the calls return when it is done (SCB's first takes ~12 minutes).
set -u

gateway="${1:-http://localhost:8081}"

# The services that have moved, and the prefix of what is still in the monolith. Step 5 of the migration guide removes
# the monolith's entry
prefixes="macro-analyzer/scrape/regional-statistics macro-analyzer"

status=0
for prefix in $prefixes; do
    code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$gateway/$prefix/enqueue-all")
    echo "POST /$prefix/enqueue-all: $code"
    [ "$code" = "202" ] || status=1
done
[ "$status" -eq 0 ] || exit "$status"

results=$(mktemp)
trap 'rm -f "$results"' EXIT
for prefix in $prefixes; do
    (
        code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$gateway/$prefix/scrape-from-queue")
        # 409: the service's cron is already draining the queue, which is what this call would have done
        echo "POST /$prefix/scrape-from-queue: $code" >> "$results"
    ) &
done
wait
cat "$results"
if grep -Ev ': (200|409)' "$results" >/dev/null; then
    exit 1
fi
