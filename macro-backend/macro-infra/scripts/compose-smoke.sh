#!/bin/sh
# Starts the real docker-compose-all.yml on an empty volume and checks each service's route through the gateway. CI only.
# A run that starts in the 22:00 UTC hour lets the crons call the real sources, as any Compose stack would, so every check
# here holds with or without a scrape. Nothing here stops the stack, as the runner is thrown away.
set -eu

if [ "${CI:-}" != "true" ]; then
    echo "compose-smoke.sh only runs in CI (CI=true). It starts docker-compose-all.yml, whose fixed ports, container names" >&2
    echo "and Compose project would collide with a local stack, re-tag its images, and could share docker_pgdata," >&2
    echo "which holds the only copy of parliament's data. Locally, use acceptance-test.sh." >&2
    exit 1
fi

gateway=http://localhost:8081
seconds_until_up=300

backend="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$backend"

compose() {
    docker compose --env-file macro-series-service/.env -f macro-infra/docker/docker-compose-all.yml "$@"
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
    deadline=$(($(date +%s) + seconds_until_up))
    until [ "$(status_of "$1")" = "$2" ]; do
        if [ "$(date +%s)" -ge "$deadline" ]; then
            fail "$1 didn't answer $2 within ${seconds_until_up}s, last answer $(status_of "$1")"
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

sh ./mvnw -B -DskipTests clean package
docker build -t regional-statistics-service:latest regional-statistics-service
docker build -t macro-series-service:latest macro-series-service
docker build -t parliament-service:latest parliament-service
docker build -t election-service:latest election-service

cp macro-series-service/.env.example macro-series-service/.env
compose up -d --build

wait_until_answers /macro-analyzer/series-config 200
wait_until_answers /macro-analyzer/parliament/parties 200
wait_until_answers /macro-analyzer/regional-statistics/indicators 200
# A year without an election, because the crons could have filled the latest one. nginx has no 204, so this reached election
wait_until_answers '/macro-analyzer/municipalities/elections?year=1900' 204

expect_json /macro-analyzer/series-config 'length' 108
expect_json /macro-analyzer/series-config '.[0].name' '"PolicyRateSweden"'
expect_json /macro-analyzer/parliament/parties 'length' 11
expect_json /macro-analyzer/parliament/parties '.[0].code' '"S"'
expect_json /macro-analyzer/regional-statistics/indicators 'length' 26
expect_json /macro-analyzer/regional-statistics/indicators 'map(.id) | index("households") != null' true

wait_until_answers /macro-analyzer/unknown 404
wait_until_answers / 200

echo "compose-smoke passed"
