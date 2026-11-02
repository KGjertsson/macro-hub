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

# Into a temporary file first, so a failure or an interrupted run leaves no file that a later run would reuse
generate_passwords() {
    local temporary value name
    mkdir -p "$(dirname "$passwords_file")"
    temporary=$(mktemp "$passwords_file.XXXXXX")
    trap 'rm -f "$temporary"' EXIT
    for name in POSTGRES_PASSWORD $db_passwords; do
        # On its own line, as set -e doesn't see a failure inside an echo's $(...)
        value=$(openssl rand -hex 24)
        [ -n "$value" ] || { echo "openssl gave no value" >&2; exit 1; }
        echo "$name=$value" >> "$temporary"
    done
    mv "$temporary" "$passwords_file"
    trap - EXIT
}

umask 077
if [ ! -e "$passwords_file" ]; then
    generate_passwords
    echo "Generated database passwords in $passwords_file"
else
    echo "Using the database passwords in $passwords_file"
fi

# shellcheck disable=SC1090
. "$passwords_file"

# An empty one would make the init script fall back on its well-known Compose default
for name in POSTGRES_PASSWORD $db_passwords; do
    [ -n "${!name:-}" ] || { echo "$passwords_file has no value for $name" >&2; exit 1; }
done

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
