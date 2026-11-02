#!/usr/bin/env bash
# Applies everything to the macro-hub namespace, in order, and waits for it. It can be re-run.
# Usage: deploy.sh [image-tag], default the current commit. build-images.sh must have imported that tag, and
# create-secrets.sh must have made the Secrets.
set -euo pipefail

here=$(cd "$(dirname "$0")" && pwd)
repo=$(git -C "$here" rev-parse --show-toplevel)
tag="${1:-$(git -C "$repo" rev-parse --short=12 HEAD)}"

# Every namespaced command goes through this, so nothing here can reach another namespace
k() { microk8s kubectl -n macro-hub "$@"; }

# The manifests with the image tag filled in
apply_images() {
    sed "s/IMAGE_TAG/$tag/" "$@" | k apply -f -
}

# A retained volume stays Released after its claim is gone, tied to that claim's uid. Keep the claim's name and namespace
# in claimRef, so only the new claim of that name can take it
release_for_same_claim() {
    if [ "$(microk8s kubectl get pv "$1" -o jsonpath='{.status.phase}' 2>/dev/null)" = "Released" ]; then
        microk8s kubectl patch pv "$1" --type=json \
            -p '[{"op":"remove","path":"/spec/claimRef/uid"},{"op":"remove","path":"/spec/claimRef/resourceVersion"}]'
    fi
}

secrets="postgres-bootstrap regional-statistics-db macro-series-db parliament-db election-db macro-series-api-keys"
for secret in $secrets; do
    k get secret "$secret" >/dev/null 2>&1 || { echo "Secret $secret is missing: run create-secrets.sh" >&2; exit 1; }
done

images=$(/snap/microk8s/current/bin/ctr --address /var/snap/microk8s/common/run/containerd.sock -n k8s.io images ls -q)
for name in regional-statistics-service macro-series-service parliament-service election-service macro-frontend; do
    grep -qxF "docker.io/macro-hub/$name:$tag" <<<"$images" \
        || { echo "macro-hub/$name:$tag isn't imported: run build-images.sh on that commit" >&2; exit 1; }
done

k apply -f "$here/namespace.yaml"
release_for_same_claim macro-hub-postgres-pv
release_for_same_claim macro-hub-backups-pv
k apply -f "$here/postgres/persistent-volume.yaml" -f "$here/postgres/persistent-volume-claim.yaml" \
    -f "$here/backup/persistent-volume.yaml" -f "$here/backup/persistent-volume-claim.yaml"

# The same init script as Compose. It only runs by itself on an empty volume
k create configmap postgres-init \
    --from-file="$repo/macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh" \
    --dry-run=client -o yaml | k apply -f -

k apply -f "$here/postgres/postgres-deployment.yaml" -f "$here/postgres/postgres-service.yaml"
k rollout status deployment/postgres --timeout=600s

# On a retained volume, the init script didn't run: make sure the databases, roles and passwords match the Secret. On any
# volume the bootstrap superuser's password follows it too
k exec -i deploy/postgres -- sh -s \
    < "$repo/macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh" >/dev/null
# \getenv keeps it off psql's command line. If the statement fails, neither the server log nor psql's message quotes it
k exec -i deploy/postgres -- sh -c 'psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres' >/dev/null <<'SQL'
\set VERBOSITY terse
\getenv password POSTGRES_PASSWORD
SET log_min_error_statement = panic;
SELECT format('ALTER ROLE %I PASSWORD %L', current_user, :'password') \gexec
SQL

apply_images "$here"/services/regional-statistics.yaml
apply_images "$here"/services/macro-series.yaml
apply_images "$here"/services/parliament.yaml
apply_images "$here"/services/election.yaml
apply_images "$here"/frontend/frontend.yaml
k apply -f "$here/backup/cronjob.yaml"

for deployment in regional-statistics-service macro-series-service parliament-service election-service macro-frontend; do
    k rollout status "deployment/$deployment" --timeout=360s
done

echo "Deployed $tag: http://localhost:30081"
