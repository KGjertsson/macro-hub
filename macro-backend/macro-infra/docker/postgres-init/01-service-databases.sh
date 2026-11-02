#!/bin/sh
# One database and role per extracted service. Mounted into /docker-entrypoint-initdb.d, which Postgres only runs on an
# empty data volume. For an existing volume, run it by hand. It can be re-run, and picks up a changed password:
#   docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh
set -eu

run_psql() {
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres "$@"
}

create_service_database() {
    run_psql -v db="$1" -v password="$2" <<'SQL'
SELECT format('CREATE ROLE %I', :'db')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = :'db') \gexec
SELECT format('ALTER ROLE %I LOGIN PASSWORD %L', :'db', :'password') \gexec
SELECT format('CREATE DATABASE %I OWNER %I', :'db', :'db')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'db') \gexec
SQL
}

create_service_database regional_statistics "${REGIONAL_STATISTICS_DB_PASSWORD:-regionalstatisticspassword}"
create_service_database macro_series "${MACRO_SERIES_DB_PASSWORD:-macroseriespassword}"
create_service_database parliament "${PARLIAMENT_DB_PASSWORD:-parliamentpassword}"

# Every role may connect to a database by default, so a service's boundary needs this on all of them: on its own
# database against the other roles, and on the others (macroanalyzer, postgres, template1) against its role.
# Owners and superusers keep their access. macrouser is a superuser, so this doesn't limit the monolith.
run_psql <<'SQL'
SELECT format('REVOKE CONNECT ON DATABASE %I FROM PUBLIC', datname)
FROM pg_database
WHERE datallowconn \gexec
SQL
