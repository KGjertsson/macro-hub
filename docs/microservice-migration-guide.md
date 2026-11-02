# Microservice migration guide

This is the living plan for splitting the backend monolith into four services: macro-series, regional-statistics, parliament and election. The decisions behind it are in [`docs/adrs/`](adrs/README.md), and the [design page](adrs/microservice-split-design.html) has the diagrams. If this guide and an ADR disagree, the ADR wins.

Statements about the current code were checked against commit `07c7482`, and the steps that are done say what changed since then. The manual trigger paths and the Flyway ordering were decided on 2026-11-01, after that check. Anything marked **Suggested** or **Open** is not decided.

## Keeping this guide updated

In the same PR as each step:

1. Tick the tasks you finished and update the status table.
2. Record what differed from the plan under "What differed" at the end of the step, even if the answer is "nothing".
3. Move answered open questions into the step they affect. If the answer is a larger decision, add an ADR.
4. Update `CLAUDE.md` so it describes what is now true.

## Starting point

Nothing is deployed anywhere. Local development uses Docker Compose, and Kubernetes is the eventual deployment target ([ADR 0006](adrs/0006-kubernetes-as-eventual-deployment-target.md)). There is no production data, downtime or live traffic to protect, and today's shared database only exists on development machines. So:

- A new database starts empty and is **re-scraped by default**. Dump and restore is only a time-saver for a local database. The exception was parliament, whose local data **was restored** while the monolith still had its tables: see "Restoring parliament".
- **Rollback for any step means reverting its PR and re-creating the local databases it changed.** Re-creating them matters: Flyway refuses to start when the database has an applied migration that is missing from the code, which is what a reverted migration looks like. Don't re-create `parliament` or delete the volume that holds it: since the monolith's V38, it is the only copy of its data.
- `docker-compose-all.yml` runs all services at each stage. The k8s files only need to keep working as far as they do today until step 6.
- Steps are still one PR each, because that keeps `main` working and each change reviewable.

## Status

| Step | Description | Status |
| --- | --- | --- |
| 0 | Record the decisions and start this guide | Done |
| 1 | Prepare inside the monolith | Done |
| 2 | Extract regional-statistics | Done |
| 3 | Extract macro-series | Done |
| 4 | Extract parliament, switch election to REST | Done |
| 5 | Retire the monolith | Not started |
| 6 | First real Kubernetes deployment | Not started |

## Open questions

| Question | What is known | Needed by |
| --- | --- | --- |
| Which is the gateway in the cluster: nginx in the frontend image, or Ingress routes? | `k8s/` has no frontend manifest. Compose keeps using nginx either way. With nginx, `BACKEND_URL`-style values need fully qualified service names. | Step 6 |
| How do images reach the cluster? | `k8s/java/deployment.yaml` uses `imagePullPolicy: Never`, with images imported by hand. | Step 6 |

## Extraction checklist

Steps 2 to 5 each use this checklist. `<service>` is the service's name, and `<db>` its database (`regional_statistics`, `macro_series`, `parliament`, `election`).

### Database and role

- [ ] Add a `create_service_database <db> "${<DB>_DB_PASSWORD:-<default>}"` line to `macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh`, and the password variable to `postgres` in both Compose files and to the service in `docker-compose-all.yml`. The script does what this SQL says, can be re-run, and picks up a changed password:

  ```sql
  CREATE ROLE <db> LOGIN PASSWORD '...';
  CREATE DATABASE <db> OWNER <db>;
  -- On every database, not only the new one
  REVOKE CONNECT ON DATABASE ... FROM PUBLIC;
  ```

  The `REVOKE` matters, because every role may connect to a database by default. On `<db>` alone it keeps the other roles out, but leaves `<db>`'s role able to connect to `macroanalyzer`, `postgres` and `template1`, so the script revokes it on all of them. Owners and superusers keep their access. `macrouser` is a superuser, so the monolith's role isn't limited by this until step 5 removes it.
- [ ] Both Compose files mount `postgres-init/`. **Postgres init scripts only run on an empty data volume**, so an existing volume needs an explicit step before the service starts, or the service fails with `database "<db>" does not exist`: `docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh`. Don't delete the volume instead: it also holds `parliament`, the only copy of its data.
- [ ] Pick the service's port and record it here:

  | Service | Port |
  | --- | --- |
  | monolith | 8080 |
  | frontend (Compose) | 8081 |
  | regional-statistics | 8082 |
  | macro-series | 8083 |
  | parliament | 8084 |
  | Vite | 5173 |

### Flyway

The monolith has no `spring.flyway.*` settings, so it uses the defaults: `classpath:db/migration` and `flyway_schema_history`.

Each service runs Flyway twice on start-up, against its own database:

1. **Platform run:** `macro-platform`'s migrations for `series_configuration` and `scrape_action_queue`, from `classpath:db/platform`, with their own history table, `flyway_platform_schema_history`. Set `macro.platform.flyway.enabled=true`.
2. **Service run:** the service's own migrations, from the default `classpath:db/migration`, with the default `flyway_schema_history`.

The platform run comes first, so the two tables exist before the service's baseline seeds its config rows. Each run has its own numbering, so no version range is needed. `spring.flyway.out-of-order=true` was rejected, because it would also weaken Flyway's ordering check on each service's own migrations. The platform's migrations stay outside `db/migration` so the monolith doesn't pick them up from the jar and try to create tables it already has.

Until step 5, `series_configuration` and `scrape_action_queue` are defined twice, in the monolith's `db/migration` and in `db/platform`, and `macro-platform`'s jOOQ model is generated from the monolith's database. A column change therefore needs a monolith migration, a new `db/platform` version and a regenerated model, in the same PR. No test catches a drift between the two unless the code uses the new column.

- [ ] Write a squashed baseline in the new service with the domain's tables and seed data, without the DDL for the two platform tables, taken from the current database (`pg_dump --schema-only -t ...` is a good start). The monolith's migrations stay there until step 5, and in git history after that.
- [ ] Include the seed data that migrations added. For parliament that was `riksdag_party`, `riksdag_election`, `riksdag_election_result` and `riksdag_organ`. Election has none: `local_governance` starts empty. Compare the seed tables with the source by checksum, and seed them in the source's physical order (`SELECT ctid, ...`): a read with no `ORDER BY` answers in that order, and an `UPDATE` in a later migration has moved rows. `RiksdagElectionRepository.readParties` is one such read.
- [ ] Copy the domain's `series_configuration` rows into the baseline, selected by `scrape_flow`. Keep `id` and `global_id` so `/series-config` keeps its order, and write `global_id` as literals so the baseline doesn't need the `uuid-ossp` extension (no column default uses it).
- [ ] Keep the names of the tables' sequences and constraints, however odd. A data-only `pg_dump` calls `setval` by the source's sequence names, so a renamed sequence fails the restore. Compare `pg_dump --schema-only` of the new database's tables with the monolith's: the diff should be empty. `pg_dump -t` leaves out functions, so a trigger's function, such as `update_updated_column`, has to be added by hand and compared on its own (`pg_get_functiondef`).
- [ ] Set the `series_configuration` sequence past the copied ids. `scrape_action_queue` needs nothing as long as the baseline copies no queue rows.
- [ ] Turn on the platform run for the service. The property goes in its `application.properties`, as it never runs without it.
- [ ] Set `spring.flyway.baseline-on-migrate=true` and `spring.flyway.baseline-version=0` for the service run. After the platform run the schema is no longer empty but `flyway_schema_history` doesn't exist yet, and Flyway then refuses to migrate ("Found non-empty schema(s) ... but no schema history table"). Version 0 is needed because Flyway skips migrations at or below the baseline, and the default baseline version of 1 would skip the service's `V1`. Both were tried on Flyway 12.4.0 and Postgres 16.

### Data

Re-scraping is the default. Dump and restore only saves time on a local database. Parliament was the exception, and was restored (see "Restoring parliament").

| Service | Re-scrape time | Notes |
| --- | --- | --- |
| regional-statistics | About 9 to 12 minutes | 2.4 M rows, 377 MB on disk. Measured 2026-11-01: 8 min 49 s (6 min 35 s fetching, 2 min writing), the container peaked at 846 MiB on a JVM with default heap settings on a 31 GiB machine. A second run with unchanged tables took 13 s. |
| macro-series | About 1 to 2 minutes (**estimate**, not a full run) | Needs the Riksbanken and Fed keys. Scaled from 7 series fetched and stored on 2026-11-01: Riksbanken 1 to 1.5 s each (39 series), Fed 0.5 to 1.3 s (11), Eurostat about 0.25 s (58), one at a time. No full run was made, and rate limits on a full run are unknown. |
| parliament | About 9 minutes | Local data was restored rather than re-scraped, and past V38 a new database can only be re-scraped: see "Restoring parliament". Measured 2026-11-02 on an empty database: 9 min 3 s, of which 16 s for 3,376 members and 8 min 47 s for 584 members' activity. Activity is only fetched for the latest two terms, so older activity in a local database isn't re-created. |
| election | About 1.5 minutes | About 310 zips. |

- [ ] Trigger the scrape through the service's manual path (`enqueue-all`, then `scrape-from-queue`), or wait for its cron during the 22:00 hour.
- [ ] Skip the queue rows on a fresh start. If you keep a local database and it has pending rows, copy them by `dataset_name` (`status = 0`; all local rows are currently done).

Optional time-saver, after Flyway has created the service's tables and **before the monolith's drop migration runs**, because that deletes what the dump reads. The monolith runs it as soon as it starts on a commit that has it, so a local database that is past it has to re-scrape. The commands below are the ones used for macro-series and regional-statistics, whose tables V36 and V34 have dropped, so they no longer work as written: that route is gone, and a new `macro_series` database can only be filled by a re-scrape. A later service names its own tables. The macro-series tables have no seed rows, so nothing needed truncating. The commands needed the service's tables at `V1`, because the monolith's tables held duplicates, which `V2`'s unique constraints reject. So the service was started once with `SPRING_FLYWAY_TARGET=1` to stop at `V1`, stopped, and restored, and the next normal start ran `V2`, which removed the duplicates from the restored copy. A later service whose tables get a constraint its source breaks needs the same order:

```bash
docker exec macrohub-postgres pg_dump -U macrouser -Fc --data-only -t 'euro_market_*' -t 'int_gov_bond_*' -t 'swedish_government_*' -t policy_rate_sweden -t exchange_usd_sek -t fed_series -t eurostat_national_debt_series macroanalyzer > ms.dump
docker exec -i macrohub-postgres pg_restore -U macro_series --no-owner -d macro_series < ms.dump
```

Tried on a throwaway stack from a dump of the local database: 41 tables, 586,918 rows, an 18 MB dump, 3 s to restore. Checksums of all 41 tables matched, and the sequences were set. The commands used for regional-statistics:

```bash
docker exec macrohub-postgres pg_dump -U macrouser -Fc --data-only -t scb_indicator -t scb_area_value -t scb_availability -t scb_table_state macroanalyzer > rs.dump
docker exec macrohub-postgres psql -U regional_statistics -d regional_statistics -c 'TRUNCATE scb_indicator, scb_table_state CASCADE'   # removes the baseline's seed rows
docker exec -i macrohub-postgres pg_restore -U regional_statistics --no-owner -d regional_statistics < rs.dump
```

Tried on a throwaway database with 2.4 M rows: a 16 MB dump and 73 s to restore. It runs as the service's role, so the foreign keys stay enforced, and `pg_restore` loads `scb_indicator` first. Counts and checksums matched, and the SCB tables have no sequences. Other services have to check their own sequences (`pg_dump -t` only includes those a named table owns, and `--data-only` then sets them).

#### Restoring parliament

**Parliament's data was restored, not re-scraped**, while the monolith's tables existed. A scrape only fetches activity for the latest two terms, members would get new `id`, `global_id` and `created` values, and `riksdag_*` is curated. An empty clone with nothing to restore fills by scraping as usual.

**V38 has dropped those tables, so a database at V38 has nothing to restore from**, and the local `parliament` database is the only copy: don't re-create it without need. A local database still before V38, with no restored `parliament` yet, has to run the block below before anything else starts the monolith on a commit that has V38. Its last start then runs V38, after the checksums have matched. Past V38 the block stops at its `pg_dump` (`no matching tables were found`), before it touches `parliament`'s rows, and leaves the monolith and `parliament` containers stopped: `docker start` both.

The six tables can't hold duplicates: `swedish_members_of_parliament` has `UNIQUE (intressent_id)`, `swedish_member_activity` and the `riksdag_*` tables have primary keys on their natural keys. So, unlike macro-series, no later constraint rejects the data, the service's `V1` doesn't need a `SPRING_FLYWAY_TARGET`, and a checksum of each whole table is a fair comparison. The one sequence, `swedish_members_of_parliament_id_seq`, is owned by the table, so `pg_dump -t` includes it, and the baseline has the same name, so `setval` works. Compare it too: it is far past the row count, because every upsert uses a value. The baseline's seed rows have to go first, or the `COPY` fails on the primary keys, and the dump's own rows replace them. `TRUNCATE` names the tables together because of the foreign keys, and `pg_dump` orders the data so that `pg_restore` loads the parents first, with the foreign keys enforced, as the service's role.

Paste this in `bash`, from the repo root of a clone on the commit that has the service. It runs in a subshell and stops at the first failure with `RESTORE FAILED` and what to do next. The only write to `macroanalyzer` is the last monolith start's migrations, after the checksums have matched: V37, which only deletes the two config rows and their queue rows, and on a commit that has it V38, which drops the tables. Everything before it writes to `parliament`, as a role that can't connect to `macroanalyzer`. The old monolith container stays stopped, not removed, until then, so `docker start` brings it back, on its old image, after a failure. The dump goes to a temporary directory outside the repo and is deleted at the end, whatever happens: the old tables are the source until V38 drops them. The block can be run again: it empties all six tables in `parliament` first. It refuses to run during the 22:00 hour (UTC in the containers), when the service's cron would start scraping into the new tables.

With `core.autocrlf=true`, a checkout of this guide has CRLF line endings, so strip the `\r` from a block taken from the file, or copy it from a rendered page.

```bash
(
set -euo pipefail
dc() { docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml "$@"; }
tables="swedish_members_of_parliament swedish_member_activity riksdag_party riksdag_election riksdag_election_result riksdag_organ"
checksums() {   # user, database
  for t in $tables; do
    docker exec macrohub-postgres psql -v ON_ERROR_STOP=1 -U "$1" -d "$2" -At -c "SELECT '$t', count(*), md5(string_agg(md5(x::text), '' ORDER BY md5(x::text))) FROM $t x"
  done
  docker exec macrohub-postgres psql -v ON_ERROR_STOP=1 -U "$1" -d "$2" -At -c "SELECT last_value, is_called FROM swedish_members_of_parliament_id_seq"
}
dump_dir=$(mktemp -d)
old_tables_step=yes
finish() {
  rc=$?
  rm -rf "$dump_dir"
  if [ "$rc" -eq 0 ]; then echo "RESTORE OK"; exit 0; fi
  echo "RESTORE FAILED (exit $rc). See the output above." >&2
  if [ "$old_tables_step" = yes ]; then
    echo "The monolith's old tables are unchanged. To go back to the old stack: docker stop macrohub-parliament; docker start macrohub-macro-analyzer" >&2
    echo "Or fix the cause and run this block again." >&2
  else
    echo "The checksums matched and parliament is restored. Only starting the new monolith or frontend failed: fix the cause and run those two lines again." >&2
  fi
  exit "$rc"
}
trap finish EXIT

[ "$(date -u +%H)" != 22 ] || { echo "Not during the 22:00 hour (UTC): the service's cron would start scraping" >&2; exit 1; }

# Build first, while nothing has stopped. Without these images, Compose would bring back the old monolith
(cd macro-backend && sh ./mvnw clean package && docker build -t macro-analyzer macro-analyzer && docker build -t parliament-service parliament-service)

# The old tables stop changing: the monolith's cron is what writes them
docker stop macrohub-macro-analyzer

# An existing volume has no parliament database (add -e PARLIAMENT_DB_PASSWORD=... after -i if .env sets it)
docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh

# Flyway creates the tables and the seed rows. Stop the service again before touching them
dc up -d --no-deps parliament
attempts=0
until [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8084/macro-analyzer/parliament/parties || true)" = 200 ]; do
  attempts=$((attempts + 1))
  if [ "$attempts" -ge 60 ]; then
    docker logs --tail 30 macrohub-parliament >&2 || true
    echo "parliament-service didn't answer within 2 minutes" >&2
    exit 1
  fi
  sleep 2
done
docker stop macrohub-parliament

docker exec macrohub-postgres pg_dump -U macrouser -Fc --data-only -t swedish_members_of_parliament -t swedish_member_activity -t riksdag_party -t riksdag_election -t riksdag_election_result -t riksdag_organ macroanalyzer > "$dump_dir/parliament.dump"
docker exec macrohub-postgres psql -v ON_ERROR_STOP=1 -U parliament -d parliament -c "TRUNCATE ${tables// /, }"
docker exec -i macrohub-postgres pg_restore -U parliament --no-owner --exit-on-error --single-transaction -d parliament < "$dump_dir/parliament.dump"

# All six tables and the sequence must match, or the monolith doesn't start
diff <(checksums macrouser macroanalyzer) <(checksums parliament parliament)
checksums parliament parliament

old_tables_step=no
docker start macrohub-parliament
dc up -d --no-deps macro-analyzer     # runs V37, and V38 on a commit that has it
dc up -d --build --no-deps macro-frontend
)
```

A failed `diff` prints the rows that differ and exits there. The `TRUNCATE` covers the four seeded tables because the baseline's rows would otherwise fail the `COPY` on the primary keys, and the two others so that a second run works. No queue rows are copied: a pending row in the old queue (`status = 0`, none locally when this was written) is simply dropped, and the next cron or manual run enqueues again.

### Code

- [ ] Create the service's Maven module, with its own `*Application` in the package `com.kg.macroanalyzer`. `@SpringBootApplication` scans from its own package, so that is what finds the `macro-platform` beans. Set `spring-boot.run.skip` to false, and add it to `<modules>` in the parent pom.
- [ ] Don't let its base `application.properties` default to the cluster or port 31274. Default to the Compose Postgres (`localhost:5432/<db>`) with the service's role, and copy `BaseConfigurationTest`, which pins that. It needs no `dev` or `prod` profile, and no `.env` import unless it has keys.
- [ ] Move the module's code. Remove it from the monolith.
- [ ] Generate a jOOQ model per service from its own database, in the package `com.kg.macroanalyzer.<module>.jooq.generated`. Give it a profile named after the service (`jooq-codegen-<service>`) that sets `jooq.codegen.url`, `.user` and `.password` to the Compose Postgres. With the shared name `jooq-codegen`, the `-am` the build needs would also run `macro-platform`'s codegen, and regenerate its model from whatever database the command names.
- [ ] Move the module's tests and fixtures. Give the service its own `ArchitectureTest` for its layers, a copy of `NoStaticResourceHandlersHttpTest` and `ManualScrapePathsHttpTest`, and an application test like `RegionalStatisticsApplicationTests`, which pins both Flyway runs.
- [ ] Update the monolith's `ArchitectureTest` (`MODULES`, `DOMAIN_MODULES`).
- [ ] Update `MonolithSeriesConfigIntegrationTest`: `shouldRouteEveryConfig_toItsDomainsScrapeFlow` lists the configs still in the monolith by name (three after step 4), so each service takes its own entries.

### Manual scrape endpoints

Every service exposes the same three endpoints, where `<service>` is `macro-series`, `regional-statistics`, `parliament` or `election`:

- `POST /macro-analyzer/scrape/<service>/enqueue?name=`
- `POST /macro-analyzer/scrape/<service>/enqueue-all`
- `POST /macro-analyzer/scrape/<service>/scrape-from-queue`

Each service gets one gateway route, `/macro-analyzer/scrape/<service>/`, so nginx needs no map of series names to services. The endpoints stay public, per [ADR 0004](adrs/0004-keep-manual-scrape-endpoints-public-for-now.md). The monolith keeps the old `/macro-analyzer/enqueue`, `/enqueue-all` and `/scrape-from-queue` for what it still holds, and they go away when the last service has moved (step 5). The script that replaces the global `enqueue-all`, `macro-backend/macro-infra/scripts/scrape-all.sh`, loops over a list of prefixes. It treats 409 as fine, because the service's cron may already have started on the queue it just filled.

- [ ] Set the service's path prefix to `macro-analyzer/scrape/<service>` (`macro.scrape.path-prefix`).
- [ ] Add the route in nginx, with the same 30-minute `proxy_read_timeout` as the other routes, and in the Vite proxy.

### Cut over

- [ ] Delete the domain's `series_configuration` rows from the monolith with a migration, **in the same PR that removes its flows**. `ScrapeService` refuses to start if a config names a flow that isn't registered.
- [ ] Delete the domain's `scrape_action_queue` rows from the monolith in the same migration. An item whose name matches no config stays pending, and fails with a warning on every cron run.
- [ ] Switch the gateway routes to the new service, in nginx and in the Vite proxy.
- [ ] Add the service to the enqueue-all replacement script.
- [ ] After verifying the service locally, drop the old tables with a monolith migration and regenerate the monolith's jOOQ model. Do this in a separate small PR, after the owner's stack has been verified. The monolith's code no longer reads them at that point. Drop a trigger's function in the same migration if nothing else uses it (`pg_trigger`; jOOQ's model doesn't show it). First compare each table in the two local databases, by checksum or row count, or by its distinct keys if the service has removed duplicates: the drop deletes the local copy, and not everything can be re-scraped (see parliament's activity under "Data"). Generate from a throwaway database at the new migration (`-Djooq.codegen.url=...`): the default, the Compose Postgres, only gets there once its monolith has restarted on the new code, and generating from it before that changes nothing. The model's diff should only delete those tables and the references to them (and `Sequences.java` or `Indexes.java`, if it has no sequence or index left), and `macro-platform`'s model, which the same command regenerates, should not change.
- [ ] Add the service to `docker-compose-all.yml`, with its image, database URL and role. CI needs no change: the `backend` job runs every module of the reactor. Add the image to the after-merge commands in `.claude/skills/ship/SKILL.md`.
- [ ] Update `CLAUDE.md`, `macro-backend/macro-analyzer/ARCHITECTURE.md`, `macro-backend/macro-analyzer/README.md` and this guide.

Note that `EnqueueService.enqueue` ignores unknown names. After a cut-over, `POST /macro-analyzer/enqueue?name=<moved series>` on the monolith still answers 202 and does nothing.

### Verify

- [ ] `mvn test` passes for the service and the monolith, and CI is green.
- [ ] The full stack starts, and the monolith logs no failed flow registration.
- [ ] Every page that uses the domain works through the gateway, and the responses match what the monolith returned. On a restored copy, compare `/municipalities/elections` and the other election responses without the order of parties that have equal votes: `LocalElectionRepository` sorts by votes only, so a restore alone reorders them.
- [ ] The role can't connect to another service's database.
- [ ] A manual scrape on `/macro-analyzer/scrape/<service>/` works through the gateway, and a second one during the run answers 409.

The Compose files set fixed container names and ports, so verify on a throwaway project, and don't give it port 31274, which can be the cluster's real database. Give it an override file that sets another `container_name`, `image` and port for each service (`ports: !override` replaces the list rather than adding to it), and run `docker compose -p <name> -f docker-compose-all.yml -f <override> up -d`. `-p` also gives it its own volume. Check the override with `docker compose ... config` first, without `--env-file`, because `config` prints the values it reads, keys included. Set `SCHEDULER_ENABLED: "false"` on each application in the override unless you are testing the cron: during the 22:00 hour (UTC in the container) it would otherwise enqueue every series, against the real APIs. Docker installed as a snap can't read `/tmp`, so the Compose files and build contexts have to be under `$HOME`.

### Rollback

Revert the PR and re-create the databases the step changed. Don't delete the volume: see "Starting point".

## Step 1: Prepare inside the monolith

**Goal:** the repo builds as a reactor and routes per domain, while everything still runs as one application.

**Tasks:**

- [x] Convert to a Maven reactor. **Suggested:** the parent `pom.xml` and `mvnw` in `macro-backend/`, with the modules `macro-platform` and `macro-analyzer`.
- [x] Extract `scrape` and `shared` into `macro-platform`, keeping their package names. `MacroAnalyzerApplication` stays in `com.kg.macroanalyzer`, so component scanning still finds them.
- [x] Give `macro-platform` its own jOOQ model, in its own package, for `series_configuration`, `scrape_action_queue` and `flyway_schema_history` (`scrape`'s repositories and `FlywayCatalogVersion` use them). Today's generated model has one package and is committed in `src/main/java`.
- [x] Adjust what depends on the Maven layout: `.github/workflows/ci.yml` (`working-directory`, `mvnw`), `macro-backend/macro-analyzer/Dockerfile` (`COPY target/...`), the image build commands in `CLAUDE.md` and `macro-backend/macro-infra/k8s/README.md`, and the other docs.
- [x] Split `location /macro-analyzer/` in `macro-frontend/nginx/default.conf.template` into per-domain routes, all still pointing at the monolith:
  - `/macro-analyzer/series-config` and `/macro-analyzer/chart-data`
  - `/macro-analyzer/regional-statistics/`
  - `/macro-analyzer/parliament/`
  - `/macro-analyzer/municipalities/`, `/macro-analyzer/regions/` and `/macro-analyzer/constituencies/`
  - `/macro-analyzer/` stays as the catch-all, which also carries the manual endpoints for now
- [x] Give each domain its own URL variable (**Suggested:** `MACRO_SERIES_URL` and so on), with a default in `macro-frontend/Dockerfile`. The nginx entrypoint only substitutes variables that are defined: an unset variable stays as the literal `${X}`, and one set to an empty string leaves `set $backend ;`, so nginx fails to start. Both are easy to hit in Compose. Keep the rule from `BACKEND_URL`: `scheme://host:port`, with no trailing slash. Keep the 30-minute `proxy_read_timeout` on every route.
- [x] **Suggested:** add a Vite proxy for `/macro-analyzer` to the monolith, split the same way, make the dev frontend call its own origin and move Vite off 8080, which ends the port clash. `rootUrl` falls back to `http://localhost:8080` when `VITE_API_URL` is unset. A `.env.development` file isn't an option: the pre-commit hook and CI reject `.env.<anything>`. Change the fallback or set the variable in the `dev` script.
- [x] Update the local start-up steps in `CLAUDE.md`, with Compose as the default route. Running a service with `mvnw` and Vite stays possible, for working on one service.

**Verify:** CI is green. The Compose stack and `npm run dev` both serve every page, and the manual endpoints still work through the gateway.

**Rollback:** revert the PR. This step changes no database.

**What differed:** all the suggested items were done as written, with these details:

- The `macro-analyzer` jOOQ model was regenerated without the three platform tables, so each table has one generated class. Its only other changes are the references to them. The two models are selected with `includes`/`excludes` in each module's `jooq-codegen` profile.
- `ArchitectureTest` stays in `macro-analyzer` and sees `macro-platform` through the classpath, as directories under `mvn test` and as a jar under `mvn package`. `importIncludesMacroPlatform` fails if that stops. `platform.jooq.generated` is not in `DOMAIN_MODULE_DEPENDENCIES`, so domain modules can't use the queue and config tables directly, which is stricter than before.
- `mvn -P jooq-codegen generate-sources` no longer works from the root, because `macro-analyzer` can't resolve `macro-platform` before it has compiled. The command is now `mvn -P jooq-codegen compile -Djooq.codegen.url=jdbc:postgresql://localhost:5432/macroanalyzer`, which names the database because the default `localhost:31274` can be the microk8s cluster's (see "Backend" in `CLAUDE.md`). The URL is the property `jooq.codegen.url` in the parent pom, which each service's profile can override instead of "the profile's URL" in the checklist. Step 2 moved that default to `localhost:5432/macroanalyzer`, so the command no longer needs the `-D`.
- Running the application from the root needs `-pl macro-analyzer -am spring-boot:run`. The parent pom sets `spring-boot.run.skip` so only the application module runs. A new service's module has to turn it off, as `macro-analyzer` does.
- `macro-analyzer/Dockerfile` needed no change, because its build context stays `macro-analyzer/` and the jar keeps its name. The image build commands changed.
- `SeriesConfigRepositoryTest` and `NoStaticResourceHandlersHttpTest` stayed in `macro-analyzer`, because they need the application and its migrations. `macro-platform` has no Flyway run until step 2. `TestSeriesConfigFactory` is copied into `macro-platform`'s tests: as a test-jar it would be imported as production code by `ArchitectureTest`.
- `location =` is used for `/macro-analyzer/series-config` and `/macro-analyzer/chart-data`, so nothing below those paths takes that route. The proxy settings, including the 30-minute `proxy_read_timeout`, are set once on the `server` block and inherited by every route. A later route only needs its own `set` and `proxy_pass`, unless it sets one of those directives itself.
- Vite moved to 5173, its default, rather than 8081, so it can run next to the Compose frontend. The Vite proxy's backends are constants in `vite.config.ts` named like the nginx variables. `rootUrl` now falls back to same origin.
- Beyond the plan: `.claude/skills/ship/SKILL.md` has the new build commands, and `CLAUDE.md` warns that microk8s' Postgres NodePort takes `localhost:31274` ahead of the Compose container when it runs.

## Step 2: Extract regional-statistics

**Goal:** regional-statistics runs as its own service with its own database. This step is the template for the others: it has no dependents and the largest runtime gain.

**Owns:** `scb_indicator`, `scb_area_value`, `scb_availability`, `scb_table_state`; flow `SCB_REGIONAL`; config `ScbRegionalStatistics`.

**Tasks:**

- [x] Make the path prefix configurable in `macro-platform`'s `InsertionAdaptor`, which has a fixed `@RequestMapping("macro-analyzer")` today. See "Manual scrape endpoints" in the checklist. The default stays `macro-analyzer`, so the monolith keeps its paths until step 5. **Suggested:** a property, `macro.scrape.path-prefix`, read with a placeholder in the mapping.
- [x] Add the first version of the script that replaces `enqueue-all`, and update the "fresh database" note in `CLAUDE.md`. It loops over the services that have moved. Until step 5 it also calls the monolith's old paths.
- [x] Add `macro-platform`'s migrations for the queue and config tables to `db/platform`.
- [x] Add the platform Flyway run to `macro-platform`, off by default. Component scanning finds `macro-platform`'s beans in the monolith too, and the monolith already has these tables from V1 to V32. **Suggested:** a `FlywayMigrationStrategy` bean (`org.springframework.boot.flyway.autoconfigure`, seen in `spring-boot-flyway` 4.1.2) enabled by a property. It migrates a second `Flyway` on the same data source, with the platform's location and history table, and then calls `migrate()` on the `Flyway` it was given.
- [x] Run the extraction checklist, except for dropping the old tables.
- [x] Add the image and its service in `docker-compose-all.yml`, and point the frontend's route variable at it. Leave room for the first scrape's heap if a memory limit is added.
- [x] Route `/macro-analyzer/regional-statistics/` and `/macro-analyzer/scrape/regional-statistics/` to the service.
- [x] Drop `scb_indicator`, `scb_area_value`, `scb_availability` and `scb_table_state` with a monolith migration, and regenerate the monolith's jOOQ model. This is a separate small PR, after the owner's stack has been verified. V33, in the step's main PR, only deletes the `ScbRegionalStatistics` config row and its queue rows, and V34 drops the tables. Removing the flow and deleting those rows have to be in the same PR, but dropping the tables doesn't.

**Verify:** the checklist's verify list, plus: the Områdesstatistik page (`/omraden`) shows data for DeSO and RegSO, and a second scrape with unchanged tables takes about 30 seconds. A running SCB scrape no longer delays a scrape in the monolith.

**Rollback:** revert the PR and re-create `macroanalyzer` and `regional_statistics`. Keep `parliament`: see "Starting point".

**What differed:** the suggested items were done as written (`macro.scrape.path-prefix`, a `FlywayMigrationStrategy` bean behind `macro.platform.flyway.enabled`, `flyway_platform_schema_history`, an init script mounted in both Compose files), with these details. The checklist above has been corrected where the template turned out wrong or incomplete.

- The old tables went in a follow-up PR, as the task above says, once the owner's stack had been verified. V34 lists them without `CASCADE` or `IF EXISTS`, so an unknown dependency fails the migration. It was applied on an empty database and on a restored dump of the local database at V33: the local database went from 464 MB to 84 MB, the two schemas came out identical, a second start migrated nothing, and both give the same jOOQ model. Its diff only deletes lines (the 8 classes, and references in `Keys`, `Public` and `Tables`), and `macro-platform`'s and the service's models didn't change. `regional-statistics-service`, on a restored copy of its database in the same Postgres, answered the 53 `/regional-statistics` requests compared like the running service.
- With `core.autocrlf=true`, `git status` lists every regenerated file as modified, because jOOQ writes LF. `git diff` shows only the real changes.
- `REVOKE CONNECT` on the new database alone didn't give the boundary the checklist asked for: the new role could still connect to `macroanalyzer`. The script revokes it on every database. This limits `macrouser` in nothing, as it is a superuser.
- The module is `regional-statistics-service`, on port 8082, inside and outside the container, so `mvnw` and Compose use the same port and Vite's proxy works with either. The Compose service is `regional-statistics`.
- The jOOQ profile is `jooq-codegen-regional-statistics`, not `jooq-codegen`, because `-pl <service> -am` would otherwise also run `macro-platform`'s codegen. `jooq.codegen.user` and `.password` became properties in the parent pom. The root command (`-P jooq-codegen`) still covers `macro-platform` and `macro-analyzer`, and leaves the service's model alone. The parent's default URL moved from `localhost:31274` to `localhost:5432/macroanalyzer`, so no codegen default can reach the cluster's database. The monolith's base properties and `dev` profile pointed at port 31274 until the follow-up described at the end of this list.
- The model's package is `regionalstatistics.jooq.generated`, inside the module, so `ArchitectureTest` needs no extra generated package for it. The service's `ArchitectureTest` only lets `adaptors.postgres` use it.
- The service's `ArchitectureTest` is a short rewrite of the monolith's, not a copy, as most of that one deals with several domain modules. It imports `macro-platform` from the classpath in the same way.
- `application-test.properties` is in `src/test/resources`, so it isn't in the jar, and uses `postgres:16` like Compose. The monolith keeps its copy in `src/main/resources` on 15.2.
- The Compose healthcheck now uses TCP (`pg_isready -h 127.0.0.1`). During an empty volume's init scripts Postgres only listens on its socket, so the old check could report healthy before `regional_statistics` existed and before the temporary server shut down.
- `macro-platform` depends on `spring-boot-starter-flyway`, for the strategy's types.
- The baseline is V29 and V30 almost verbatim, not `pg_dump` output, so it keeps their comments and readable JSON. `pg_dump --schema-only` of the two databases' six shared tables is identical apart from the history tables, and the checksum of `scb_indicator` matches. The config row's `global_id` is the local database's.
- The monolith's tests lost `ScbRegionalStatistics` from `ChartSeriesConfigIntegrationTest` and `MacroSeriesServiceTest`. `ScrapeServiceTest` in `macro-platform` keeps the name, as a mock flow.
- CI needed no change.
- Measured on a real first scrape through the gateway, on a fresh database: 8 min 49 s, faster than the estimated 12 minutes, 2,401,042 rows, and the same data as the local database in all 613 `/regional-statistics` requests compared. A second run with unchanged tables took 13 s, faster than the estimated 30. A run of `MembersOfParliament` in the monolith during the SCB run took its normal 20 s.
- After this step, the monolith's defaults moved off the cluster too, because its `dev` profile had reached the owner's real database through microk8s' NodePort on `localhost:31274`. Its base properties and `dev` profile now use `localhost:5432/macroanalyzer`, pinned by its own `BaseConfigurationTest`, and `docker-compose-dependencies.yml` publishes 5432 instead of 31274. `k8s/java/deployment.yaml` had relied on the base properties, so it now sets `SPRING_DATASOURCE_URL` to the same address. The two Compose files now define an identical `postgres` service (see "Starting the local stack" in `CLAUDE.md`). Port 31274 is still the cluster's NodePort, for anyone who wants it on purpose.

## Step 3: Extract macro-series

**Goal:** macro-series runs as its own service. This is the largest move, but it should be mechanical with the template.

**Owns:** the 41 rate, bond, bill, exchange-rate, Fed and Eurostat tables; flow `MACRO_POINT`; 108 configs.

**Tasks:**

- [x] Run the extraction checklist, except for dropping the old tables.
- [x] Move the `RIKSBANKEN_PROD_SUBSCRIPTION_KEY` and `FED_PROD_SUBSCRIPTION_KEY` settings to this service only, in `docker-compose-all.yml` and the service's `.env.example`.
- [x] Move `ChartSeriesConfigIntegrationTest` here, including its count of 108 and its category set. Move its checks on other flows to their owners.
- [x] Route `/macro-analyzer/series-config`, `/macro-analyzer/chart-data` and `/macro-analyzer/scrape/macro-series/` to the service. Add the image and its service in `docker-compose-all.yml`.
- [x] Drop the 41 old tables with a monolith migration, and regenerate the monolith's jOOQ model. This is a separate small PR, after the owner's stack has been verified. V35, in the step's main PR, only deletes the `MACRO_POINT` config rows and their queue rows, and V36 drops the tables.

**Verify:** the checklist's verify list, plus: `/series-config` lists 108 entries in the same order, and charts for a Riksbanken, a Fed and a Eurostat series render.

**Rollback:** revert the PR and re-create `macroanalyzer` and `macro_series`. Keep `parliament`: see "Starting point".

**What differed:** the template held, with these details. The checklist above has been corrected where it turned out incomplete.

- The module is `macro-series-service`, on port 8083 inside and outside the container. The Compose service is `macro-series`, and the jOOQ profile is `jooq-codegen-macro-series`. V35 is the monolith's migration. The model was generated from a throwaway database. Its profile selects `.*` minus the platform's tables, including `flyway_platform_schema_history`, rather than listing 41 names.
- The local `.env` moved to `macro-backend/macro-series-service/.env`, next to the service that uses the keys and in a directory that step 5's rename doesn't touch. Compose reads it with `--env-file`, and Spring from the working directory under `spring-boot:run`, both tried with a Fed series that needs the key. An existing clone has to move its file. The monolith's `spring.config.import` and key properties are gone, as nothing there reads them. The optional database passwords are in the same file, so there is one `--env-file` for every Compose command.
- The baseline is `pg_dump --schema-only` output rewritten per table, not the monolith's migrations, because those rename tables repeatedly. `pg_dump` of the result is identical to the monolith's for all 41 tables, including the old sequence and constraint names such as `euro_market_3_month_denmark_id_seq` and `swedish_government_bonds_7_month_id_seq` on `int_gov_bond_*` and `swedish_government_bonds_7_year`. Those names have to stay, because the dump's `setval` uses them.
- `id` and order: `SeriesConfigRepository` reads in `id` order. `/series-config` uses that order, and `MacroSeriesService` filters that same list, so `/chart-data` returns series, and `ColorSelectionStrategy` gives colours, in `id` order whatever the request's order is. The baseline keeps the 108 ids (1 to 110, with gaps), which keeps both. `created` and `updated` of the config rows aren't copied, as no response uses them.
- One intended change in behaviour: a `/chart-data` request naming a config of another domain (`MembersOfParliament`) answered 500 in the monolith, whose config list held it, and now is treated like any unknown name (204, or 200 with the other series). The frontend only sends names from `/series-config`.
- Compared through the new gateway with the running stack, on a restored copy of the local database: `/series-config` byte for byte (108 entries, 33,182 bytes), and 2,310 `/chart-data` requests across every series, the 5 categories, the 13 subcategories, reversed orders and mixed sources, each with all 3 strategies and 5 time frames. All were identical in status and bytes: 1,881 answered 200 with real data and 429 answered 204 (a short time frame on a series with no point that recent, such as Eurostat's yearly debt figures). The 400 cases have the same status and differ only in their timestamp.
- A real scrape of 7 series on an empty database stored the same data as the local database for 6 of them. `FEDDJ` differs at the start only: FRED serves the Dow Jones and S&P 500 for a rolling 10 years, so a re-scrape loses days a restored dump keeps. A second `scrape-from-queue` during the run answered 409.
- The local database has every Fed series except `FEDEffectiveFederalFundsRate` about twice over (`FEDCPI`: 1,050 rows, 525 dates), from before this step. The identical copies don't affect charts, as sampling keeps one point per date, and those responses match a clean scrape's. On the 32 dates with revised values in `FEDPublicDebtAsGDPPercentage` and `FEDSurplusOrDeficit`, charts showed the oldest value, which a clean scrape doesn't store. A restore copied the duplicates and a re-scrape doesn't have them. The service's `V2` has since removed them, added a unique constraint per series and date to all 41 tables, and made the write an upsert, so a revised value replaces the old row. This affects later steps only as a pattern: a table that is scraped repeatedly should have its natural key as a constraint from the start.
- A stack that upgrades without `--no-deps` re-creates `macrohub-postgres`, on the same volume, because the service's password variable is new in its environment. That is harmless, but it restarts every service's connections.
- The monolith's and the service's `TestJsonReader` differ: the monolith's lost `readMacroSeriesList`. `ChartSeriesConfigIntegrationTest`'s checks of other domains' configs moved to `MonolithSeriesConfigIntegrationTest`.
- `scrape-all.sh` was only run against a stub gateway here, which saw all six calls: the point of this step's verification was not to call the external APIs for all 108 series.
- CI needed no change.
- The old tables went in a follow-up PR, once the owner's stack had been verified. V36 lists all 41 without `CASCADE` or `IF EXISTS`. Before the service's `V2` removed the duplicates, the local `macroanalyzer` and `macro_series` databases held the same row count in each table (586,918 in all). After it, `macro_series` has 417,886 rows, and the set of series and dates in each of the 41 tables is the same as the monolith's (compared by keys, not values), so V36 loses no date. V36 was applied on an empty database and on a restored dump of the local database at V35: the copy went from 95 MB to 19 MB, the two schemas came out identical, a second start migrated nothing, and both give the same jOOQ model.
- The model's diff only deletes lines, in 87 files: the 82 table and record classes, references in `Keys`, `Indexes`, `Public` and `Tables`, and `Sequences.java`. That file only held 21 sequences of these tables, with old names such as `euro_market_10_year_denmark_id_seq`, so jOOQ no longer generates it. `macro-platform`'s and the services' models didn't change.
- The monolith's model is regenerated with `compile`, as `CLAUDE.md` says. `generate-sources` fails on `macro-analyzer`, because `macro-platform` isn't built by then.
- On the restored copy at V36, 401 of 403 parliament and election requests matched the running stack (at V35) byte for byte, with every kommun, region, valkrets, election year and 40 members. `/municipalities/elections`, with and without `?year=2026`, differed only in the order of parties with equal votes in 144 of 290 kommuner. A copy restored at V35, without V36, differs from the stack in the same way, in 201 kommuner, so V36 isn't the cause: the repository sorts by votes only. The checklist's verify list now says so. Fixing the order is out of scope.

## Step 4: Extract parliament, and switch election to REST

**Goal:** parliament runs as its own service, and election no longer depends on its code. See [ADR 0005](adrs/0005-cached-rest-between-services-and-no-message-broker.md). The election switch can be its own PR before the extraction, while election and parliament are still both in the monolith.

**Owns:** `swedish_members_of_parliament`, `swedish_member_activity`, `riksdag_party`, `riksdag_election`, `riksdag_election_result`, `riksdag_organ`; flows `MEMBER_OF_PARLIAMENT` and `MEMBER_ACTIVITY`; configs `MembersOfParliament` and `MembersOfParliamentActivity`.

**Tasks:**

- [x] Give election its own out-port for party colours, and use it in `LocalElectionService` in place of `PartyDirectory`.
- [x] Add the HTTP adaptor for `GET /macro-analyzer/parliament/parties`, with a configurable URL, a background refresh and the fallbacks from ADR 0005. It must accept the 204 the endpoint gives for an empty list, and must not block start-up. While both are in the monolith, the configurable URL points at the monolith itself (`http://localhost:8080`).
- [x] Add a contract test that pins `code` and `color`, and update `LocalElectionServiceTest`.
- [x] Remove the `election` entry from `DOMAIN_MODULE_EXTRA_DEPENDENCIES` in `ArchitectureTest`, and remove `PartyDirectory` from parliament.
- [x] Run the extraction checklist for parliament, except for dropping the old tables.
- [x] Route `/macro-analyzer/parliament/` and `/macro-analyzer/scrape/parliament/` to the service, and point election's URL at it. Add the image and its service in `docker-compose-all.yml`.
- [x] Drop the six old tables with a monolith migration, and regenerate the monolith's jOOQ model. This is a separate small PR, after the owner's stack has been verified: until then they are what "Restoring parliament" reads. V37, in the step's main PR, only deletes the two config rows and their queue rows, and V38 drops the tables.

**Verify:** the checklist's verify list, plus: with parliament stopped, election pages still answer and keep Riksdag colours after a refresh fails. With parliament stopped and election just restarted, election pages still answer, with Valmyndigheten's colours.

**Rollback:** revert the PR and re-create `macroanalyzer`, and keep `parliament`: since V38 it is the only copy. Reverting only the drop needs nothing more, as nothing reads the re-created tables. To go back to before the extraction, load the six tables from `parliament` into the re-created `macroanalyzer` first, as "Restoring parliament" does in the other direction.

**What differed:**

Election's switch to REST:

- The out-port is `PartyColorPort` (colour by party code). `RefreshingPartyColorAdaptor` implements it from memory and `ParliamentPartiesClient` makes the call, so a request only reads a field. Election imports nothing from parliament.
- ADR 0005 left the interval open. It is 1 hour (`election.parliament.refresh-interval`), and 15 seconds (`retry-interval`) until the first list has loaded. After that a failed refresh keeps the list and waits for the next hourly one. The whole exchange has a 5 second deadline (`timeout`), on the future rather than `HttpRequest.timeout`, which doesn't cover a body that stalls after the headers.
- The first refresh runs on `ApplicationReadyEvent`, when the application listens, because in the monolith the endpoint is the application itself. On a restored copy the list had loaded 0.4 seconds after the application had started.
- The refresh has its own daemon thread, not `@Scheduled`: `SchedulerConfig` turns `@Scheduled` off with `scheduler.enabled=false`, which would have left election on Valmyndigheten's colours.
- A 204, or an empty list, doesn't replace a list already held, like a failed refresh. `riksdag_party` is only written by migrations, so an empty answer points to a broken parliament, not to parties having gone away. A 200 that isn't a list of parties with a `code` and a `color` counts as a failed refresh. With no list yet, both keep retrying.
- The contract is a file, `macro-backend/contracts/parliament-parties.json`, with only the two fields election reads. `macro-analyzer`'s `pom.xml` puts it on the test classpath. `ParliamentPartiesClientTest` holds election to it, and `PartiesContractHttpTest` holds the real endpoint to it, on the migrated database. Neither imports the other side, so the second test moves with parliament. **The extraction has to add the same `testResource` to the new service's `pom.xml`.**
- The test profile points the URL at a closed port, with a retry of an hour, so tests can't read a real stack on 8080. `PartyColorsAtStartupHttpTest` runs the real wiring against a stub.
- Compose sets `ELECTION_PARLIAMENT_URL: http://macro-analyzer:8080`, which the extraction changes to the new service. `k8s/` isn't changed: the default, the application's own port on `localhost`, holds there while parliament is in the monolith.
- `ParliamentServiceTest` tested `listParties`, which is gone, so it now tests `getParties`.
- Compared with the running stack on a restored copy of the local database: 690 requests over every kommun, region and valkrets (the database only has 2026), with and without `?year=`, and the unknown year and area cases. 688 were identical in status and bytes. The two `/municipalities/elections` lists differed only in the order of parties with equal votes, as in step 3.
- With a stub for parliament: a closed port, a server that never answers, a body that stalls, 500, 204 and HTML with no list yet all gave start-ups of the same 8 seconds and fast answers with Valmyndigheten's colours (SD `#4E83A3`). With a list held, the same failures and a stopped stub kept the stub's colours, and a stub-only colour for SD proved they come from REST.

The extraction:

- The template held. The module is `parliament-service`, on port 8084 inside and outside the container, the Compose service is `parliament`, and the jOOQ profile is `jooq-codegen-parliament`. V37 is the monolith's migration. The model was generated from a throwaway database. CI needed no change.
- The tables moved with a new trigger function: `pg_dump -t` doesn't include `update_updated_column`, which the members table's trigger needs, so the baseline defines it. The checklist now says so. `pg_dump --schema-only` of the six tables is identical to the monolith's apart from the owner. The function differs only in line endings, because the monolith's migration was applied from a CRLF checkout.
- `readParties` has no `ORDER BY`, and V24's `UPDATE` left SD last in the table. V22's seed order with V24 folded in would have moved SD on an empty database, with no difference in a checksum. The baseline seeds SD last, `ParliamentApplicationTests` pins it, and the checklist now says to look at the physical order. `/parliament/parties` and `/parliament/elections` on an empty database are byte for byte the running stack's.
- **Parliament is restored**, unlike the two earlier services, as "Restoring parliament" describes. That block was run as written on a throwaway stack, only with the container names and ports replaced, starting from a copy of the local database at V36 and the old `macro-analyzer` image: all six checksums and the sequence (243072) matched, a 2.6 MB dump, under a second to restore, and the new monolith then ran V37 and loaded the colours 0.4 seconds after starting. There are no duplicates to consider, because every table has its natural key as a constraint.
- The block's failures were tried the same way, with its image builds replaced by throwaway tags. A failed build stopped before anything else. A wrong `PARLIAMENT_DB_PASSWORD` in `.env` gave `RESTORE FAILED` with the service's log after 2 minutes. A checksum altered on purpose printed the differing row and stopped before the monolith started. In each case `macroanalyzer` stayed at V36 with 5 configs and the same checksum, the documented `docker stop` and `docker start` brought the old monolith back on its old image with `/parliament/parties` answering, and no dump was left behind. Running the block again after the failed checksum ended in `RESTORE OK`, with V37 applied and the old tables' checksum unchanged.
- What a re-scrape would have changed, measured on an empty database: today, all 584 activity rows come back with the same values, keys and JSON, and 3,374 of 3,376 members have the same `member_data`, while `id`, `global_id`, `created`, `updated` and the sequence differ, so whole-table checksums differ. The local database holds no activity older than the latest two terms right now: all 584 rows were written by one run on 2026-11-01. So restoring protects little activity today. It does keep the members' identifiers, and the point about activity returns once a term has changed.
- Compared through the new gateway with the running stack, on a restored copy: all 17 `/parliament/*` list requests (parties, elections, composition, members for all 11 election years, from 384 to 458 members each, and the unknown year and member cases), 295 member details (every third of the 584 members with activity, and every 28th of the other 2,792) and 690 election requests (every kommun, region and valkrets, with and without `?year=`, and the unknown cases) had the same status and bytes, 1,002 requests and 10 MB in all, except the two `/municipalities/elections` lists, which differed only in the order of parties with equal votes. Member details were a sample because each takes about a second on the running stack; the whole-table checksums cover the rest of the data they read.
- This was the first real use of the Compose service name in `ELECTION_PARLIAMENT_URL`. On a start of the whole stack, the monolith was ready before parliament, its first attempt failed, and the next, 15 seconds later, logged `Loaded 11 party colours`. The monolith has no `depends_on` on parliament; the frontend does.
- With the `parliament` container stopped, on a restored copy with `ELECTION_PARLIAMENT_REFRESH_INTERVAL=20s`: refreshes failed and logged `keeping the 11 held`, and `/municipalities/elections` kept answering 200 in about 0.1 s with SD `#FFCC00` and S `#E8112d`. After restarting the monolith with parliament still stopped, election answered 13 s after the restart, with Valmyndigheten's colours (SD `#4E83A3`, S `#FF0000`). After `docker start` of parliament, the colours were back 25 s later, 13 s after parliament logged that it had started; the retry interval of 15 s is the upper bound on that part. `/macro-analyzer/parliament/*` answers 502 through the gateway while it is stopped.
- Election's default URL had to change, because this application no longer serves `/parliament/parties`. It is `http://localhost:8084`, pinned by the monolith's `BaseConfigurationTest`, and run under `mvnw` with both applications it loaded the colours from the service. The test profile still points at a closed port. `k8s/` isn't changed apart from a comment: it has no parliament manifest, so in a pod the default answers nothing, `/parliament/*` answers 404, and election uses Valmyndigheten's colours and logs a warning every 15 seconds until step 6.
- `MonolithSeriesConfigIntegrationTest` now lists the three election configs, and its check of empty chart metadata uses `MunicipalElections`. `ParliamentApplicationTests` has the Riksdagen configs' version. The monolith's `TestSeriesConfigFactory` had no user left and is gone. The `PartiesContractHttpTest` and its contract `testResource` moved with the service, and the monolith keeps the same `testResource` for `ParliamentPartiesClientTest`.
- A real scrape through the gateway on an empty database: `enqueue-all` answered 202, `scrape-from-queue` answered 200 after 9 min 3 s, both queue rows were done, and a second call during the run answered 409. The estimate of about 7 minutes for activity was 8 min 47 s.
- The role `parliament` is refused on `macroanalyzer`, `postgres`, `template1`, `macro_series` and `regional_statistics`, and the roles `macro_series` and `regional_statistics` on `parliament`. All four applications started on empty databases.
- `npm run dev` was checked with curl, with `parliament-service` under `mvnw` on 8084 and a throwaway database: `/parliament/*` and a `POST` to `/scrape/parliament/enqueue` reached the service (seen in its log), and `/municipalities/*` and `/series-config` reached the running stack with GET. No page was opened in a browser. `scrape-all.sh` was only run against a stub gateway, which saw all eight calls: the parliament scrape's one real run had already been used.

The follow-up that dropped the old tables:

- V38 lists all six without `CASCADE` or `IF EXISTS`, `riksdag_election_result` first because of its foreign keys. Just before writing it, the local `macroanalyzer` and `parliament` databases had the same row count and checksum in all six tables (3,376 members, 584 activity rows, 11 parties, 11 elections, 81 results, 43 organs) and the same sequence value (243072), so no scrape had run in `parliament` since the restore.
- `update_updated_column()` was only used by the members table's trigger (`pg_trigger`, and V19 is the only migration that names it), so V38 drops it too, also without `CASCADE`. `parliament-service`'s baseline has its own. The monolith's database now has no trigger or function of its own, so step 5's baseline needs none. The `uuid-ossp` extension stays until the database goes in step 5.
- V38 was applied on an empty database and on a restored dump of the local database at V37: the copy went from 19 MB to 9.4 MB, the two schemas came out identical, a second start migrated nothing, and both give the same jOOQ model.
- The model's diff is 16 files: the 12 table and record classes and `Indexes.java`, whose only index was on these tables, are deleted, and `Keys`, `Public` and `Tables` lose their references. Its one added line is a comma moving in `Public`. jOOQ never generated a class for the trigger function, so dropping it changes nothing there. `macro-platform`'s and the services' models didn't change.
- On the restored copy at V38, with colours from the running `parliament`, all 690 election requests (every kommun, region and valkrets, with and without `?year=`, and the unknown cases) matched the running stack (at V37) byte for byte, except the two `/municipalities/elections` lists, which differ only in the order of parties with equal votes. With no parliament reachable, election answered with Valmyndigheten's colours (SD `#4E83A3`, S `#FF0000`), and an empty database at V38 answered 204.

## Step 5: Retire the monolith

**Goal:** what remains of the monolith is election, and it becomes a normal service. No trace of the monolith is left.

**Owns:** `local_election_area`, `local_election_party_result`, `local_governance`; flow `LOCAL_ELECTION`; configs `MunicipalElections`, `RegionElections` and `ParliamentElections`.

**Tasks:**

- [ ] Run the extraction checklist for election, with the new database `election`. Skip the cut-over items that delete config rows, delete queue rows and drop old tables with a monolith migration: the monolith itself becomes election, so they have no target. The old tables go away with the `macroanalyzer` database.
- [ ] Rename the `macro-analyzer` module, application class, image, jar and Compose service to election.
- [ ] Delete the monolith's migrations (V1 to V38) and the old jOOQ model from the tree. They stay in git history.
- [ ] Route `/macro-analyzer/scrape/election/` to election. Its new prefix removes the old `/macro-analyzer/enqueue`, `/enqueue-all` and `/scrape-from-queue`, so remove the monolith's call from the script, which now only loops over the four names. Every service now sets its own prefix, so remove the `macro-analyzer` default.
- [ ] Change the catch-all `location /macro-analyzer/` in nginx to answer 404. Without it, unknown API paths would fall through to `location /` and get `index.html` with a 200.
- [ ] Remove `BACKEND_URL`, the `macroanalyzer` database from `POSTGRES_DB`, and the `macrouser` settings no service uses.
- [ ] Rewrite the architecture and build sections of `CLAUDE.md`, and retire or rewrite `macro-backend/macro-analyzer/ARCHITECTURE.md`.

**Verify:** the checklist's verify list, plus: on a clean checkout with empty volumes (a throwaway project, as under "Verify", not the volume that holds `parliament`), `docker-compose-all.yml` starts all four services, the script fills them and every page works.

**Rollback:** revert the PR and re-create `macroanalyzer` and `election`, and re-scrape election. Keep the volume and the other services' databases: `parliament` is the only copy of its data.

**What differed:** not started.

## Step 6: First real Kubernetes deployment

**Goal:** the app runs in a cluster for the first time. This step is only an outline. It needs its own design when it starts.

It comes last here, but it doesn't depend on the split being finished, so it can move earlier. The k8s files aren't kept in step with the services before this, so expect them to need catching up.

**Tasks:**

- [ ] Fix or replace the existing manifests in `macro-backend/macro-infra/k8s/`. `java/service.yaml` selects a label the pods don't have, `ingress/test-ingress.yaml` names a Service that doesn't exist, and `postgres/postgres-deployment.yaml` uses `postgres:13` while Compose uses `postgres:16`.
- [ ] Add manifests for all four services, with database settings and API keys, and `ELECTION_PARLIAMENT_URL` on election (until then its default, `localhost:8084`, answers nothing in a pod, so it uses Valmyndigheten's colours). **Open:** how secrets are stored. `postgres-configmap.yaml` holds the password in plain text today.
- [ ] Add routing. **Open:** a frontend manifest with nginx (using fully qualified service names as `CLAUDE.md` describes), or Ingress routes.
- [ ] Create the Postgres databases and roles. **Open:** how, given that init scripts only run on an empty volume.
- [ ] Set up image delivery for four images plus the frontend. **Open:** keep `imagePullPolicy: Never` with hand-imported images, or use a registry.
- [ ] Revisit [ADR 0004](adrs/0004-keep-manual-scrape-endpoints-public-for-now.md), because the manual scrape endpoints become reachable by whoever can reach the gateway.
- [ ] **Open:** backups per database, which [ADR 0003](adrs/0003-one-database-per-service-on-one-postgres-instance.md) notes. The repo has none today.
- [ ] **Open:** memory requests and limits, including room for SCB's first full scrape. Measure against the estimate in ADR 0001.
- [ ] Decide whether Kafka stays in `kubernetes_setup.sh`. No application code uses it.

**Verify:** every page works through the cluster's gateway, and each service's first scrape completes.

**Rollback:** remove the deployment. Nothing depends on it yet.

**What differed:** not started.
