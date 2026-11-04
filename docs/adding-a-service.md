# Adding a service

The backend is one service per domain, so a new domain is a new service. This checklist covers what surrounds the module. "Adding a new domain" in [`CLAUDE.md`](../CLAUDE.md) covers what goes inside it, and `parliament-service` and `election-service` are the templates.

`<service>` is the service's name, and `<db>` its database and role (`macro_series`, for example).

## Database and role

- [ ] Add a `create_service_database <db> "${<DB>_DB_PASSWORD:-<default>}"` line to `macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh`, and the password variable to `postgres` in both Compose files, to the service in `docker-compose-all.yml`, and as a comment to `macro-backend/macro-series-service/.env.example`, which lists every service's optional password. The script creates the role and the database, revokes `CONNECT` from `PUBLIC` on every database, can be re-run, and picks up a changed password.
- [ ] Init scripts only run on an empty volume. An existing volume needs the script run by hand before the service starts, or the service fails with `database "<db>" does not exist`: `docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh`. **Don't delete the volume instead:** it holds `parliament`, the only copy of its data.
- [ ] Pick the next free port (the table at the top of `CLAUDE.md` lists the ones in use).

## Flyway

Each service runs Flyway twice on start-up, against its own database. First `macro-platform`'s `db/platform`, which creates `series_configuration` and `scrape_action_queue` with its own history table and needs no setting. Then the service's `db/migration`.

- [ ] Write a `V1` with the domain's tables and its `series_configuration` rows, without the DDL for the two platform tables. Write `global_id` as literals, so it doesn't need the `uuid-ossp` extension.
- [ ] Set `spring.flyway.baseline-on-migrate=true` and `spring.flyway.baseline-version=0`. After the platform run the schema isn't empty but the service's history table doesn't exist, so Flyway otherwise refuses to migrate ("Found non-empty schema(s)"). A baseline at the default version 1 would skip the service's `V1`.
- [ ] Never edit an applied migration, even a comment: Flyway's checksum covers comments, and an existing database then fails validation. That is why some baselines still mention the monolith.

A column change in a platform table needs a new `db/platform` version and a regenerated `macro-platform` model in the same PR (profile `jooq-codegen-platform`).

## Code

- [ ] Create the Maven module, with its `*Application` in the package `com.kg.macroanalyzer`, which is what finds `macro-platform`'s beans. Set `spring-boot.run.skip` to false, and add the module to `<modules>` in the parent pom.
- [ ] Give the module a `Dockerfile`, with its jar name and `EXPOSE`, as `election-service/Dockerfile` has. Set `server.port` to the port you picked: without it the service silently starts on 8080.
- [ ] Default its `application.properties` to the Compose Postgres (`localhost:5432/<db>`) with the service's role. It needs no profile besides `test`, and no `.env` import unless it has keys.
- [ ] Set `spring.jooq.sql-dialect=POSTGRES` and the three `spring.datasource.hikari.*` lines. Without them it starts on Hikari's default of 10 connections, all kept open, which breaks the sum under "Database" in `CLAUDE.md`.
- [ ] Generate a jOOQ model from its own database, in the package `com.kg.macroanalyzer.<module>.jooq.generated`. Give it a profile named `jooq-codegen-<service>` that sets `jooq.codegen.url`, `.user` and `.password` to the Compose Postgres: the parent pom has no default. Its `<excludes>` must list `series_configuration|scrape_action_queue|flyway_schema_history|flyway_platform_schema_history`, or those tables get generated into the service's model as well, as `macro-platform`'s model holds them. The database must already be at the latest migration, so start the service once before generating. Code using the model can't compile until then.

## Manual scrape endpoints and the gateway

- [ ] Set `macro.scrape.path-prefix` to `macro-analyzer/scrape/<service>`. It has no default, so a service without it fails at start-up with `Could not resolve placeholder`. The service then has `enqueue?name=`, `enqueue-all` and `scrape-from-queue` under that path, public like the others ([ADR 0004](adrs/0004-keep-manual-scrape-endpoints-public-for-now.md)).
- [ ] Add its routes to `macro-frontend/nginx/default.conf.template`, including `/macro-analyzer/scrape/<service>/`, with their own URL variable. The variable needs a default in `macro-frontend/Dockerfile`. The proxy settings, with the 30-minute timeout, are inherited from the `server`.
- [ ] Add the same routes to the proxy in `macro-frontend/vite.config.ts`, above the catch-all `^/macro-analyzer(/|\?|$)` entry. The first match wins, so a route added after it answers 404.
- [ ] Add the service's name to `services` in `macro-backend/macro-infra/scripts/scrape-all.sh`.

## Compose and docs

- [ ] Add the service to `docker-compose-all.yml`, with its image, database URL and role. Add the new URL variable to `macro-frontend`'s `environment`, as a literal value, and the service to that container's `depends_on`. The `backend` CI job needs no change, because it runs every module of the reactor. The `acceptance` job only runs what `acceptance-test.sh` builds, see "Tests" below.
- [ ] Add the image to the build commands in `CLAUDE.md` and `README.md`, and to the after-merge commands in `.claude/skills/ship/SKILL.md`.
- [ ] Update `CLAUDE.md`, `macro-backend/ARCHITECTURE.md` and `docs/architecture.md`, and give the service its own README.

## Tests

- [ ] Give the service its own `ArchitectureTest` for its layers, and copies of `BaseConfigurationTest`, `NoStaticResourceHandlersHttpTest`, `ManualScrapePathsHttpTest` and `ConnectionPoolIntegrationTest`.
- [ ] Add `src/test/resources/application-test.properties` with a TestContainers datasource, like the other services'. Without it, `@ActiveProfiles("test")` falls back to the base properties, so the copied tests run Flyway and write against the Compose Postgres.
- [ ] Add an application test like `RegionalStatisticsApplicationTests`, which pins both Flyway runs.
- [ ] When a journey in `macro-backend/acceptance-tests` needs the service, add a `BackendService` constant with its gateway URL variable, put it in `AcceptanceStack.SERVICES`, and add a `docker build` line to `macro-backend/macro-infra/scripts/acceptance-test.sh` ([ADR 0008](adrs/0008-black-box-acceptance-tests-through-the-gateway.md)).

## Data

A new database starts empty and is filled by a scrape. Use the service's manual paths (`enqueue-all`, then `scrape-from-queue`), or wait for its cron during the 22:00 hour (UTC in the container).

## Verify

- [ ] `mvn test` passes and CI is green.
- [ ] The full stack starts, and every page that uses the domain works through the gateway.
- [ ] The role can't connect to another service's database.
- [ ] A manual scrape on `/macro-analyzer/scrape/<service>/` works through the gateway, and a second one during the run answers 409.

The Compose files set fixed container names and ports, so verify on a throwaway project. Give it an override file that sets another `container_name`, `image` and port for each service (`ports: !override` replaces the list rather than adding to it), and run `docker compose -p <name> -f docker-compose-all.yml -f <override> up -d`. `-p` also gives it its own volume. Check the override with `docker compose ... config` first, without `--env-file`, because `config` prints the values it reads, keys included. Set `SCHEDULER_ENABLED: "false"` on each application in the override unless you are testing the cron: during the 22:00 hour it would otherwise enqueue every series, against the real APIs. Docker installed as a snap can't read `/tmp`, so the Compose files and build contexts have to be under `$HOME`.

## Rollback

Revert the PR and drop the new database and role. Flyway refuses to start when a database has an applied migration that is missing from the code, so a database that ran a reverted migration has to be re-created. Don't delete the volume.
