#!/bin/sh
# For postgres_exporter. Postgres only runs this on an empty data volume. For an existing volume, run it by hand. It can
# be re-run, and picks up a changed password:
#   docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/02-monitoring-role.sh
set -eu

# \getenv keeps the password off psql's command line. If a statement fails, neither the server log nor psql's message
# quotes it. Connecting to postgres is all the role needs: pg_monitor shows every database's statistics from there, and
# 01-service-databases.sh keeps it out of the service databases and their tables
MONITORING_DB_PASSWORD="${MONITORING_DB_PASSWORD:-monitoringpassword}" \
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<'SQL'
\set VERBOSITY terse
\getenv password MONITORING_DB_PASSWORD
SET log_min_error_statement = panic;
SELECT 'CREATE ROLE monitoring'
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'monitoring') \gexec
SELECT format('ALTER ROLE monitoring LOGIN PASSWORD %L', :'password') \gexec
GRANT pg_monitor TO monitoring;
GRANT CONNECT ON DATABASE postgres TO monitoring;
SQL
