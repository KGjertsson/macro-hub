#!/bin/sh
set -eu

K6_IMAGE=grafana/k6:2.3.0
COMPOSE_NETWORK=docker_default
LOCAL_GATEWAY=http://macro-frontend

usage() {
    cat >&2 <<USAGE
Usage: load-test.sh [--allow-remote] [smoke|load|stress] [gateway]

Runs a k6 load test of the endpoints the frontend calls, through the gateway. It only reads, but it is real load on
that stack: see "Load tests" in CLAUDE.md. Profile defaults to smoke, gateway to $LOCAL_GATEWAY on $COMPOSE_NETWORK.
Any other host than macro-frontend, localhost or 127.0.0.1 needs --allow-remote.
USAGE
    exit 2
}

fail() {
    echo "load-test: $1" >&2
    exit 1
}

allow_remote=false
if [ "${1:-}" = "--allow-remote" ]; then
    allow_remote=true
    shift
fi
[ "$#" -le 2 ] || usage
case "${1:-}" in
    -h|--help) usage ;;
esac

profile="${1:-smoke}"
gateway="${2:-$LOCAL_GATEWAY}"
gateway="${gateway%/}"

case "$profile" in
    smoke|load|stress) ;;
    *) fail "unknown profile \"$profile\": use smoke, load or stress" ;;
esac

# Only scheme, host and port, so that a URL like http://localhost@elsewhere can't get past the host check below
echo "$gateway" | grep -Eq '^https?://[A-Za-z0-9.-]+(:[0-9]+)?$' || fail "\"$gateway\" is not of the form http://host[:port]"

host="${gateway#*://}"
host="${host%%:*}"
case "$host" in
    macro-frontend|localhost|127.0.0.1) ;;
    *)
        [ "$allow_remote" = true ] || fail "refusing to load \"$gateway\": only macro-frontend, localhost and 127.0.0.1 are allowed without --allow-remote"
        ;;
esac

docker info >/dev/null 2>&1 || fail "Docker isn't running"

if [ "$host" = "macro-frontend" ]; then
    docker network inspect "$COMPOSE_NETWORK" >/dev/null 2>&1 || fail "network $COMPOSE_NETWORK doesn't exist: start the app stack first (docker-compose-all.yml)"
    network="$COMPOSE_NETWORK"
    prometheus_host=prometheus
else
    network=host
    prometheus_host=127.0.0.1
fi

scripts="$(cd "$(dirname "$0")/../load-tests" && pwd)"

set --

# POST with no body: 404 means the receiver is off, any other answer means it listens
prometheus_status="$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 -X POST http://127.0.0.1:9090/api/v1/write || true)"
prometheus_env=""
case "$prometheus_status" in
    000)
        echo "No Prometheus on 127.0.0.1:9090: results only in the summary below."
        ;;
    404)
        echo "Prometheus runs without its remote-write receiver, so re-create it: docker compose -f macro-backend/macro-infra/docker/docker-compose-observability.yml up -d --no-deps prometheus. Results only in the summary below."
        ;;
    *)
        echo "Prometheus found on 127.0.0.1:9090: results also go to the dashboard Macro Hub Load Tests (http://localhost:3000)."
        # Native histograms because k6's Trend statistics are computed over the whole run, not per window, so they can't
        # show a latency that changes during a stress ramp
        prometheus_env="-e K6_PROMETHEUS_RW_SERVER_URL=http://$prometheus_host:9090/api/v1/write -e K6_PROMETHEUS_RW_STALE_MARKERS=true -e K6_FEATURES=native-histograms"
        set -- --out experimental-prometheus-rw
        ;;
esac

echo "Running the $profile profile against $gateway (network $network)."

# Without a terminal k6 logs a progress line per second, which buries the summary
terminal_flag=""
quiet_flag="--quiet"
if [ -t 1 ]; then
    terminal_flag="-t"
    quiet_flag=""
fi

# shellcheck disable=SC2086
exec docker run --rm $terminal_flag \
    --network "$network" \
    -v "$scripts:/load-tests:ro" \
    -e GATEWAY="$gateway" \
    -e PROFILE="$profile" \
    -e LOAD_TEST_SMOKE_ROUNDS \
    -e LOAD_TEST_RATE \
    -e LOAD_TEST_DURATION \
    -e LOAD_TEST_MAX_VUS \
    -e LOAD_TEST_STRESS_MAX_RATE \
    -e LOAD_TEST_STRESS_STEP_SECONDS \
    -e LOAD_TEST_P95_MS \
    -e LOAD_TEST_ERROR_RATE \
    $prometheus_env \
    "$K6_IMAGE" run $quiet_flag "$@" /load-tests/load-test.js
