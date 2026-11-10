#!/bin/sh
# Builds the jars and the images the acceptance tests run, then runs the tests. CI runs this too.
# The images get their own :acceptance tag, so this can't change what a local Compose stack starts.
# Usage: acceptance-test.sh [maven arguments], for example -Dit.test=MacroSeriesAcceptanceTest
set -eu

backend="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$backend"

# Pulled while the images build, so the test classes don't all wait on the same pull when they start. Testcontainers
# pulls only an image that isn't there. The tags are read from AcceptanceStack, so a bump there can't skip this
stack_source=acceptance-tests/src/test/java/com/kg/macroanalyzer/acceptance/support/AcceptanceStack.java
image_of() {
    image="$(sed -n "s/.* $1 = \"\(.*\)\";.*/\1/p" "$stack_source")"
    if [ -z "$image" ]; then
        echo "No $1 in $stack_source" >&2
        exit 1
    fi
    echo "$image"
}
postgres_image="$(image_of POSTGRES_IMAGE)"
wiremock_image="$(image_of WIREMOCK_IMAGE)"
docker pull -q "$postgres_image" >/dev/null &
postgres_pull=$!
docker pull -q "$wiremock_image" >/dev/null &
wiremock_pull=$!

sh ./mvnw -B -DskipTests clean package
docker build -t regional-statistics-service:acceptance regional-statistics-service
docker build -t macro-series-service:acceptance macro-series-service
docker build -t parliament-service:acceptance parliament-service
docker build -t election-service:acceptance election-service
docker build -t auth-service:acceptance auth-service
docker build -t macro-frontend:acceptance ../macro-frontend
wait "$postgres_pull"
wait "$wiremock_pull"
sh ./mvnw -B -pl acceptance-tests verify "$@"
