# 0003. One database per service on one Postgres instance

## Status

Accepted

## Date

2026-11-01

## Context

The data is already nearly partitioned. The foreign keys in the migrations (V22, V23, V29, V30) all stay inside one domain, and the only tables used across domains are `series_configuration` and `scrape_action_queue`. Those split cleanly: configs by `scrape_flow`, queue rows by `dataset_name`.

The app isn't deployed anywhere ([0006](0006-kubernetes-as-eventual-deployment-target.md)), so there is no production data to move. Today's shared database only exists on development machines.

## Decision

Each service gets its own database and its own role, all on one Postgres instance. The databases are `macro_series`, `regional_statistics`, `parliament` and `election`.

The draft design recommended a schema and role per service. The project owner chose a database per service instead.

*Note, 2026-11-02:* every service now has its database (`regional_statistics`, `macro_series`, `parliament`, `election`). A new Postgres volume has no other application database. Its one bootstrap superuser, `macrouser`, is only for the init script and maintenance: no service logs in with it. Its name stays because an existing volume has that role.

*Note, 2026-11-02:* the first deployment has backups: a nightly `pg_dump` per database in the `macro-hub` namespace, with the restore in `k8s/README.md`. Its Postgres is a new one, on a new volume, re-scraped rather than restored, with the same init script. Compose's databases have no backups and stay as they were.

*Note, 2026-11-04:* [0007](0007-remove-the-kubernetes-deployment-until-its-needed.md) removed that deployment and its backups, so no database here has a backup now.

## Alternatives considered

- **Shared tables.** Rejected, because services would break each other through schema changes.
- **Schema and role per service, one instance.** This was the draft's recommendation. Migrating is easier (`ALTER TABLE ... SET SCHEMA`, with no data copied) and there is one backup, but the boundary is weaker: it is only grants, one `GRANT` away from a cross-domain query.
- **Instance per service.** Rejected as too heavy for this data: four Postgres processes and four volumes, where one `docker stats` reading of the local process showed 555 MiB.

## Consequences

- Postgres itself blocks cross-domain queries, because a query can't reach another database.
- Restoring one domain is simple, and a service moves to its own server by moving its database and changing its URL.
- A new database starts empty. The default is to re-scrape into it. Dump and restore per domain is only a time-saver for a local database, mainly because a first SCB scrape takes about 12 minutes.
- Backups are per database. The repo has no backup tooling today, so this becomes a task when the app is first deployed.
- New databases allow connections from every role by default, so each role's boundary needs `REVOKE CONNECT ... FROM PUBLIC`.
- Postgres init scripts only run on an empty data volume, so existing volumes need an explicit step to create the databases and roles.
- The instance stays a shared point of failure, and a heavy SCB write can still slow the other services.
- The jOOQ `inputSchema` stays `public` in every service, and each service generates its model from its own database.
