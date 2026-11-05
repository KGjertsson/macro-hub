#!/bin/sh
# Starts the real docker-compose-all.yml on an empty volume and checks each service's route through the gateway. CI only.
# compose-smoke.override.yml only turns the crons off, so nothing can call a real source, and the script fails before
# starting anything if a service lacks that. Nothing here stops the stack, as the runner is thrown away.
set -eu

if [ "${CI:-}" != "true" ]; then
    echo "compose-smoke.sh only runs in CI (CI=true). It starts docker-compose-all.yml, whose fixed ports, container names" >&2
    echo "and Compose project would collide with a local stack, re-tag its images, and could share docker_pgdata," >&2
    echo "which holds the only copy of parliament's data. Locally, use acceptance-test.sh." >&2
    exit 1
fi

# CI=true doesn't prove a throwaway runner: act sets it too, and uses the host's Docker. These never exist on a fresh runner
if docker volume inspect docker_pgdata >/dev/null 2>&1 || docker container inspect macrohub-postgres >/dev/null 2>&1; then
    echo "compose-smoke.sh refuses to run: docker_pgdata or macrohub-postgres exists, so this Docker holds a local stack." >&2
    echo "docker_pgdata holds the only copy of parliament's data, and this script is only for a fresh CI runner." >&2
    exit 1
fi

gateway=http://localhost:8081
seconds_until_up=300

backend="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$backend"

compose() {
    docker compose --env-file macro-series-service/.env \
        -f macro-infra/docker/docker-compose-all.yml \
        -f macro-infra/scripts/compose-smoke.override.yml "$@"
}

fail() {
    echo "compose-smoke failed: $1" >&2
    exit 1
}

dump_logs_on_failure() {
    status=$?
    if [ "$status" -ne 0 ]; then
        compose ps -a || true
        compose logs --no-color || true
    fi
    exit "$status"
}
trap dump_logs_on_failure EXIT

status_of() {
    curl -s -o /dev/null -w '%{http_code}' --max-time 15 "$gateway$1" || true
}

wait_until_answers() {
    until [ "$(status_of "$1")" = "$2" ]; do
        if [ "$(date +%s)" -ge "$deadline" ]; then
            fail "$1 didn't answer $2 within ${seconds_until_up}s of the start, last answer $(status_of "$1")"
        fi
        sleep 2
    done
    echo "ok: $1 answers $2"
}

expect_json() {
    actual="$(curl -s --max-time 15 "$gateway$1" | jq -c "$2")"
    if [ "$actual" != "$3" ]; then
        fail "$1 | jq '$2': expected $3, got $actual"
    fi
    echo "ok: $1 | jq '$2' is $3"
}

cp macro-series-service/.env.example macro-series-service/.env

services_with_crons="$(compose config --format json | jq -c '[.services | to_entries[]
    | select(.key != "postgres" and .key != "macro-frontend" and .value.environment.SCHEDULER_ENABLED != "false")
    | .key]')"
if [ "$services_with_crons" != "[]" ]; then
    fail "add SCHEDULER_ENABLED to compose-smoke.override.yml for $services_with_crons, or its crons could call real sources"
fi

sh ./mvnw -B -DskipTests clean package
docker build -t regional-statistics-service:latest regional-statistics-service
docker build -t macro-series-service:latest macro-series-service
docker build -t parliament-service:latest parliament-service
docker build -t election-service:latest election-service

compose up -d --build

# One deadline for all the waits, so the job's own timeout can't kill the script before it dumps the logs
deadline=$(($(date +%s) + seconds_until_up))

wait_until_answers /macro-analyzer/series-config 200
wait_until_answers /macro-analyzer/parliament/parties 200
wait_until_answers /macro-analyzer/regional-statistics/indicators 200
# nginx has no 204, so this reached election
wait_until_answers /macro-analyzer/municipalities/elections 204

expect_json /macro-analyzer/series-config 'length' 108
expect_json /macro-analyzer/series-config '.[0].name' '"PolicyRateSweden"'
expect_json /macro-analyzer/parliament/parties 'length' 11
expect_json /macro-analyzer/parliament/parties '.[0].code' '"S"'
expect_json /macro-analyzer/regional-statistics/indicators 'length' 26
expect_json /macro-analyzer/regional-statistics/indicators 'map(.id) | index("households") != null' true

wait_until_answers /macro-analyzer/unknown 404
wait_until_answers / 200

echo "compose-smoke passed"
