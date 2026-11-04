#!/bin/sh
# Builds the jars and the images the acceptance tests run, then runs the tests. CI runs this too.
# The images get their own :acceptance tag, so this can't change what a local Compose stack starts.
# Usage: acceptance-test.sh [maven arguments], for example -Dit.test=MacroSeriesScrapeAcceptanceTest
set -eu

backend="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$backend"

sh ./mvnw -B -DskipTests clean package
docker build -t macro-series-service:acceptance macro-series-service
docker build -t macro-frontend:acceptance ../macro-frontend
sh ./mvnw -B -pl acceptance-tests verify "$@"
