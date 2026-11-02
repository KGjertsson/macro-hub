#!/usr/bin/env bash
# Removes this deployment from the macro-hub namespace, by name. The namespace, both retained PersistentVolumes, their
# data under /mnt/data/macro-hub and the local passwords file stay, so deploy.sh brings back the same data.
set -euo pipefail

here=$(cd "$(dirname "$0")" && pwd)

k() { microk8s kubectl -n macro-hub "$@"; }

k delete --ignore-not-found \
    -f "$here/backup/cronjob.yaml" \
    -f "$here/frontend/frontend.yaml" \
    -f "$here/services/regional-statistics.yaml" \
    -f "$here/services/macro-series.yaml" \
    -f "$here/services/parliament.yaml" \
    -f "$here/services/election.yaml" \
    -f "$here/postgres/postgres-service.yaml" \
    -f "$here/postgres/postgres-deployment.yaml"

# The Deployments are gone, but their pods take a moment to stop, and a claim stays until no pod uses it
k delete --ignore-not-found -f "$here/postgres/persistent-volume-claim.yaml" -f "$here/backup/persistent-volume-claim.yaml"

k delete --ignore-not-found configmap postgres-init
k delete --ignore-not-found secret postgres-bootstrap regional-statistics-db macro-series-db parliament-db election-db \
    macro-series-api-keys

echo "Removed. Kept: namespace macro-hub, PersistentVolumes macro-hub-postgres-pv and macro-hub-backups-pv, /mnt/data/macro-hub"
