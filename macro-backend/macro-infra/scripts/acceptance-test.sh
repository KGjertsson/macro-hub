#!/bin/sh
# Builds the jars and the images the acceptance tests run, then runs the tests. CI runs this too.
# The images get their own :acceptance tag, so this can't change what a local Compose stack starts.
# Usage: acceptance-test.sh [maven arguments], for example -Dit.test=MacroSeriesAcceptanceTest
set -eu

backend="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$backend"

# Pulled while the images build, so the test classes don't all wait on the same pull when they start. The tags are
# AcceptanceStack's POSTGRES_IMAGE and WIREMOCK_IMAGE; Testcontainers pulls only an image that isn't there
docker pull -q postgres:16 >/dev/null &
postgres_pull=$!
docker pull -q wiremock/wiremock:3.13.2 >/dev/null &
wiremock_pull=$!

sh ./mvnw -B -DskipTests clean package
docker build -t regional-statistics-service:acceptance regional-statistics-service
docker build -t macro-series-service:acceptance macro-series-service
docker build -t parliament-service:acceptance parliament-service
docker build -t election-service:acceptance election-service
docker build -t macro-frontend:acceptance ../macro-frontend
wait "$postgres_pull"
wait "$wiremock_pull"
sh ./mvnw -B -pl acceptance-tests verify "$@"
