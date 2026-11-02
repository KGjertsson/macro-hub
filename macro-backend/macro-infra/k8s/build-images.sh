#!/usr/bin/env bash
# Builds the four services and the frontend from the current commit, tags them macro-hub/<name>:<commit> and imports them
# into microk8s' containerd. There is no registry, so the manifests use imagePullPolicy: Never.
#
# It builds from `git archive HEAD`, so the tag says exactly what is in the image: uncommitted changes aren't included.
# The copy lives under $HOME because Docker as a snap can't read /tmp, nor a hidden directory in $HOME.
# Compose's images (<name>:latest) aren't touched.
# Needs JDK 25 (JAVA_HOME, default ~/.sdkman/candidates/java/current), Docker and membership of the microk8s group.
set -euo pipefail

repo=$(git -C "$(dirname "$0")" rev-parse --show-toplevel)
tag=$(git -C "$repo" rev-parse --short=12 HEAD)
export JAVA_HOME="${JAVA_HOME:-$HOME/.sdkman/candidates/java/current}"

services="regional-statistics-service macro-series-service parliament-service election-service"

if [ -n "$(git -C "$repo" status --porcelain)" ]; then
    echo "Note: the working tree has uncommitted changes, which the images won't contain" >&2
fi

work=$(mktemp -d "$HOME/macro-hub-image-build.XXXXXX")
trap 'rm -rf "$work"' EXIT
git -C "$repo" archive HEAD macro-backend macro-frontend | tar -x -C "$work"

echo "Building jars for $tag"
(cd "$work/macro-backend" && ./mvnw -q -DskipTests package)

build_and_import() {
    local name=$1 context=$2
    echo "Building macro-hub/$name:$tag"
    docker build -q -t "macro-hub/$name:$tag" "$context" >/dev/null
    echo "Importing macro-hub/$name:$tag"
    # `microk8s images import` rather than `microk8s ctr image import`, which asks for sudo
    docker save "macro-hub/$name:$tag" | microk8s images import >/dev/null
}

for service in $services; do
    build_and_import "$service" "$work/macro-backend/$service"
done
build_and_import macro-frontend "$work/macro-frontend"

echo "Built and imported $tag. Deploy it with: $(dirname "$0")/deploy.sh $tag"
