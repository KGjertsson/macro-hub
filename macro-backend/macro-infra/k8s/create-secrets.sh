#!/usr/bin/env bash
# Creates or updates the Secrets in the macro-hub namespace. Prints no values.
# Usage: create-secrets.sh [env-file], default macro-backend/macro-series-service/.env
#
# The API keys come from the env file (RIKSBANKEN_PROD_SUBSCRIPTION_KEY, FED_PROD_SUBSCRIPTION_KEY). The database passwords
# are generated once and kept outside the repo in $MACRO_HUB_K8S_PASSWORDS_FILE, default
# ~/.config/macro-hub/k8s-db-passwords.env, mode 600. They must stay the same for as long as the retained volume exists,
# because the roles on it have them: keep that file. The README says what to do if it is lost.
set -euo pipefail

here=$(cd "$(dirname "$0")" && pwd)
repo=$(git -C "$here" rev-parse --show-toplevel)
env_file="${1:-$repo/macro-backend/macro-series-service/.env}"
passwords_file="${MACRO_HUB_K8S_PASSWORDS_FILE:-$HOME/.config/macro-hub/k8s-db-passwords.env}"

api_keys="RIKSBANKEN_PROD_SUBSCRIPTION_KEY FED_PROD_SUBSCRIPTION_KEY"
db_passwords="REGIONAL_STATISTICS_DB_PASSWORD MACRO_SERIES_DB_PASSWORD PARLIAMENT_DB_PASSWORD ELECTION_DB_PASSWORD"

# Reads a Secret's literal KEY=value lines from stdin. The values stay out of every command line
apply_secret() {
    microk8s kubectl -n macro-hub create secret generic "$1" --from-env-file=/dev/stdin --dry-run=client -o yaml \
        | microk8s kubectl -n macro-hub apply --server-side --force-conflicts -f - >/dev/null
    echo "Secret $1"
}

[ -r "$env_file" ] || { echo "Can't read $env_file" >&2; exit 1; }
for key in $api_keys; do
    grep -Eq "^$key=.+" "$env_file" || { echo "$env_file has no value for $key" >&2; exit 1; }
done

microk8s kubectl -n macro-hub apply -f "$here/namespace.yaml" >/dev/null

umask 077
if [ ! -e "$passwords_file" ]; then
    mkdir -p "$(dirname "$passwords_file")"
    {
        echo "POSTGRES_PASSWORD=$(openssl rand -hex 24)"
        for name in $db_passwords; do
            echo "$name=$(openssl rand -hex 24)"
        done
    } > "$passwords_file"
    echo "Generated database passwords in $passwords_file"
else
    echo "Using the database passwords in $passwords_file"
fi

# shellcheck disable=SC1090
. "$passwords_file"

{
    printf 'POSTGRES_PASSWORD=%s\n' "$POSTGRES_PASSWORD"
    printf 'REGIONAL_STATISTICS_DB_PASSWORD=%s\n' "$REGIONAL_STATISTICS_DB_PASSWORD"
    printf 'MACRO_SERIES_DB_PASSWORD=%s\n' "$MACRO_SERIES_DB_PASSWORD"
    printf 'PARLIAMENT_DB_PASSWORD=%s\n' "$PARLIAMENT_DB_PASSWORD"
    printf 'ELECTION_DB_PASSWORD=%s\n' "$ELECTION_DB_PASSWORD"
} | apply_secret postgres-bootstrap

# Each service only gets its own password
printf 'password=%s\n' "$REGIONAL_STATISTICS_DB_PASSWORD" | apply_secret regional-statistics-db
printf 'password=%s\n' "$MACRO_SERIES_DB_PASSWORD" | apply_secret macro-series-db
printf 'password=%s\n' "$PARLIAMENT_DB_PASSWORD" | apply_secret parliament-db
printf 'password=%s\n' "$ELECTION_DB_PASSWORD" | apply_secret election-db

# Only macro-series gets the API keys
grep -E "^(RIKSBANKEN_PROD_SUBSCRIPTION_KEY|FED_PROD_SUBSCRIPTION_KEY)=" "$env_file" | apply_secret macro-series-api-keys
