# Microservice migration guide

This is the living plan for splitting the backend monolith into four services: macro-series, regional-statistics, parliament and election. All steps are done: the monolith is gone, the app runs in a cluster, and `CLAUDE.md` describes what is there now. The decisions behind it are in [`docs/adrs/`](adrs/README.md), and the [design page](adrs/microservice-split-design.html) has the diagrams. If this guide and an ADR disagree, the ADR wins.

Statements about the current code were checked against commit `07c7482`, and the steps that are done say what changed since then. The manual trigger paths and the Flyway ordering were decided on 2026-11-01, after that check. Anything marked **Suggested** or **Open** is not decided.

## Keeping this guide updated

In the same PR as each step:

1. Tick the tasks you finished and update the status table.
2. Record what differed from the plan under "What differed" at the end of the step, even if the answer is "nothing".
3. Move answered open questions into the step they affect. If the answer is a larger decision, add an ADR.
4. Update `CLAUDE.md` so it describes what is now true.

## Starting point

When this guide started, nothing was deployed anywhere, and the steps up to 5 were planned on that basis. Local development uses Docker Compose, and Kubernetes is the eventual deployment target ([ADR 0006](adrs/0006-kubernetes-as-eventual-deployment-target.md)). There is no production data, downtime or live traffic to protect, and today's shared database only exists on development machines. So:

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
| 5 | Retire the monolith | Done |
| 6 | First real Kubernetes deployment | Done |

## Open questions

None for the migration. The project owner's question about how far the gateway's network exposure reaches is recorded in [ADR 0004](adrs/0004-keep-manual-scrape-endpoints-public-for-now.md).

## Extraction checklist

Steps 2 to 5 each used this checklist, and a new service can use it as it stands. Step 5 has retired the monolith, so an item that names it, `macro-analyzer` or `macroanalyzer` describes how the work was done, not something still to do: the new service has no monolith to update. `<service>` is the service's name, and `<db>` its database (`regional_statistics`, `macro_series`, `parliament`, `election`).

### Database and role

- [ ] Add a `create_service_database <db> "${<DB>_DB_PASSWORD:-<default>}"` line to `macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh`, and the password variable to `postgres` in both Compose files and to the service in `docker-compose-all.yml`. The script does what this SQL says, can be re-run, and picks up a changed password:

  ```sql
  CREATE ROLE <db> LOGIN PASSWORD '...';
  CREATE DATABASE <db> OWNER <db>;
  -- On every database, not only the new one
  REVOKE CONNECT ON DATABASE ... FROM PUBLIC;
  ```

  The `REVOKE` matters, because every role may connect to a database by default. On `<db>` alone it keeps the other roles out, but leaves `<db>`'s role able to connect to `postgres` and `template1`, so the script revokes it on all of them. Owners and superusers keep their access. `POSTGRES_USER` (`macrouser`) is the image's bootstrap superuser, which only the script and maintenance use.
- [ ] Both Compose files mount `postgres-init/`. **Postgres init scripts only run on an empty data volume**, so an existing volume needs an explicit step before the service starts, or the service fails with `database "<db>" does not exist`: `docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh`. Don't delete the volume instead: it also holds `parliament`, the only copy of its data.
- [ ] Pick the service's port and record it here:

  | Service | Port |
  | --- | --- |
  | frontend (Compose) | 8081 |
  | regional-statistics | 8082 |
  | macro-series | 8083 |
  | parliament | 8084 |
  | election | 8085 |
  | Vite | 5173 |

### Flyway

Each service runs Flyway twice on start-up, against its own database:

1. **Platform run:** `macro-platform`'s migrations for `series_configuration` and `scrape_action_queue`, from `classpath:db/platform`, with their own history table, `flyway_platform_schema_history`. It needs no setting: since step 5 every service runs it.
2. **Service run:** the service's own migrations, from the default `classpath:db/migration`, with the default `flyway_schema_history`.

The platform run comes first, so the two tables exist before the service's baseline seeds its config rows. Each run has its own numbering, so no version range is needed. `spring.flyway.out-of-order=true` was rejected, because it would also weaken Flyway's ordering check on each service's own migrations. The platform's migrations stay in `db/platform`, outside each service's `db/migration`.

`series_configuration` and `scrape_action_queue` are defined once, in `db/platform`. A column change needs a new `db/platform` version and a regenerated `macro-platform` model, in the same PR, and every service picks it up at its next start. The model is generated by the profile `jooq-codegen-platform` from any service's database. The default is `localhost:5432/election`, and `includes` selects the three tables, so `flyway_platform_schema_history` doesn't get into it. `flyway_schema_history` does, because `FlywayCatalogVersion` reads it, and it exists in every service's database, from the service run. **Don't edit `db/platform/V1` or a service's `V1`, even a comment:** Flyway's checksum covers the comments, and an existing database then fails validation. That is why a few baselines still mention the monolith.

- [ ] Write a squashed baseline in the new service with the domain's tables and seed data, without the DDL for the two platform tables, taken from the current database (`pg_dump --schema-only -t ...` is a good start). The monolith's migrations stay there until step 5, and in git history after that.
- [ ] Include the seed data that migrations added. For parliament that was `riksdag_party`, `riksdag_election`, `riksdag_election_result` and `riksdag_organ`. Election has none: `local_governance` starts empty. Compare the seed tables with the source by checksum, and seed them in the source's physical order (`SELECT ctid, ...`): a read with no `ORDER BY` answers in that order, and an `UPDATE` in a later migration has moved rows. `RiksdagElectionRepository.readParties` is one such read.
- [ ] Copy the domain's `series_configuration` rows into the baseline, selected by `scrape_flow`. Keep `id` and `global_id` so `/series-config` keeps its order, and write `global_id` as literals so the baseline doesn't need the `uuid-ossp` extension (no column default uses it).
- [ ] Keep the names of the tables' sequences and constraints, however odd. A data-only `pg_dump` calls `setval` by the source's sequence names, so a renamed sequence fails the restore. Compare `pg_dump --schema-only` of the new database's tables with the monolith's: the diff should be empty. `pg_dump -t` leaves out functions, so a trigger's function, such as `update_updated_column`, has to be added by hand and compared on its own (`pg_get_functiondef`).
- [ ] Set the `series_configuration` sequence past the copied ids. `scrape_action_queue` needs nothing as long as the baseline copies no queue rows.
- [ ] Set `spring.flyway.baseline-on-migrate=true` and `spring.flyway.baseline-version=0` for the service run. After the platform run the schema is no longer empty but `flyway_schema_history` doesn't exist yet, and Flyway then refuses to migrate ("Found non-empty schema(s) ... but no schema history table"). Version 0 is needed because Flyway skips migrations at or below the baseline, and the default baseline version of 1 would skip the service's `V1`. Both were tried on Flyway 12.4.0 and Postgres 16.

### Data

Re-scraping is the default. Dump and restore only saves time on a local database. Parliament was the exception, and was restored (see "Restoring parliament").

| Service | Re-scrape time | Notes |
| --- | --- | --- |
| regional-statistics | About 9 to 12 minutes | 2.4 M rows, 377 MB on disk. Measured 2026-11-01: 8 min 49 s (6 min 35 s fetching, 2 min writing), the container peaked at 846 MiB on a JVM with default heap settings on a 31 GiB machine. A second run with unchanged tables took 13 s. |
| macro-series | About 1 to 2 minutes (measured 2026-11-02: 51 s) | Needs the Riksbanken and Fed keys. Scaled from 7 series fetched and stored on 2026-11-01: Riksbanken 1 to 1.5 s each (39 series), Fed 0.5 to 1.3 s (11), Eurostat about 0.25 s (58), one at a time. The full run in step 5 hit no rate limit. |
| parliament | About 9 minutes | Local data was restored rather than re-scraped, and past V38 a new database can only be re-scraped: see "Restoring parliament". Measured 2026-11-02 on an empty database: 9 min 3 s, of which 16 s for 3,376 members and 8 min 47 s for 584 members' activity. Activity is only fetched for the latest two terms, so older activity in a local database isn't re-created. |
| election | About 1.5 minutes | About 310 zips. Local data can be restored: see "Restoring election". Measured 2026-11-02 on an empty database, next to the other three scrapes: 2 min 7 s, 339 areas, 6,733 party rows. With every file unchanged it takes under a second. |

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

*Past step 5, this block can't run as written: it builds `macro-analyzer`, which is gone. It only holds on a commit before step 5, and past V38 it has nothing to restore from anyway. It stays as the record of what was done, and as the template for "Restoring election".*

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

#### Restoring election

**The election tables can be re-scraped in about 1.5 minutes**, so restoring isn't needed for the results to come back. It is still what a local stack should do, because it keeps the `source_md5` values, which let the first scrape skip every unchanged file, and it copies anything in `local_governance`, which has no source but migrations (it is empty today). Nothing is lost by skipping this on a stack that has no election data worth keeping, but the `macroanalyzer` database should then be dropped by hand, as below.

Run this on a stack that still runs the old monolith, from the repo root of a clone on the commit that has `election-service`. The block:

- runs in a subshell, and stops at the first failure with `RESTORE FAILED` and how to go back, or with `RESTORE OK`;
- refuses to run during the 22:00 hour (UTC in the containers), or five minutes before it, when election's cron would start scraping into the new tables. It checks again after the builds, which take several minutes;
- builds everything first, while nothing has stopped: all four service images, because `macro-platform` changed, and the frontend. A failed build leaves the old stack as it was;
- only reads `macroanalyzer`, as `macrouser`. Everything it writes goes to `election`, as the role `election`, which can't connect to `macroanalyzer`;
- gates on a checksum of the three tables and the three config rows (the latter without `created` and `updated`, which aren't copied): the old data and the new must match, or the gateway isn't switched;
- keeps the dump in a temporary directory outside the repo and deletes it at the end, whatever happens;
- keeps the old `macrohub-macro-analyzer` container stopped, not removed, until everything has worked, and removes it as its last step. It keeps the old frontend's image as `macro-frontend:before-step5`, for going back. The `macro-analyzer:latest` image stays too, so Compose can bring the container back even after that;
- can be run again after a failure: it empties the three tables in `election` first. It refuses once `macrohub-macro-analyzer` is gone, because by then `election` is the live data, which a second restore would overwrite;
- does **not** drop `macroanalyzer`. That is a separate command, below.

`--no-deps` is still needed on every `up`, as in the block: without it Compose re-creates `macrohub-postgres`, because its environment changed. That is safe for the data, since the volume stays and an existing volume ignores `POSTGRES_*`, and a service with a connection pool answers 500 for a few seconds and reconnects by itself (see "Database" in `CLAUDE.md`). An image built before that pool, which step 5's stack was, instead **answers 500 until it is restarted**.

Postgres doesn't need the monolith's database any more: a new volume doesn't create it, and an existing volume keeps working once it is dropped (see below).

No queue rows are copied: a pending row in the old queue (`status = 0`, none locally when this was written) is dropped, and the next cron or manual run enqueues again. If `.env` sets `ELECTION_DB_PASSWORD`, add `-e ELECTION_DB_PASSWORD=...` after `-i` on the line that runs the init script, because the running `macrohub-postgres` doesn't have that variable yet. Otherwise the service can't log in and the block fails after 2 minutes, with the service's log.

With `core.autocrlf=true`, a checkout of this guide has CRLF line endings, and bash fails on the `\r`s. Copy the block from a rendered page, or take it from the file with the `\r`s stripped, then run the result with `bash`:

```bash
tr -d '\r' < docs/microservice-migration-guide.md | awk '/^#### Restoring election/{s=1} /^#### Dropping the old database/{s=0} s&&/^```bash$/{n++; if(n==2){f=1;next}} /^```$/{f=0} f' > "$HOME/restore-election.sh"
```

```bash
(
set -euo pipefail
env_file=macro-backend/macro-series-service/.env
dc() { docker compose --env-file "$env_file" -f macro-backend/macro-infra/docker/docker-compose-all.yml "$@"; }
tables="local_election_area local_election_party_result local_governance"
checksums() {   # user, database
  for t in $tables; do
    docker exec macrohub-postgres psql -v ON_ERROR_STOP=1 -U "$1" -d "$2" -At -c "SELECT '$t', count(*), md5(string_agg(md5(x::text), '' ORDER BY md5(x::text))) FROM $t x"
  done
  docker exec macrohub-postgres psql -v ON_ERROR_STOP=1 -U "$1" -d "$2" -At -c "SELECT 'series_configuration', count(*), md5(string_agg(md5((id, global_id, name, display_name, scrape_url, category, unit, subcategory, country, maturity_months, scrape_flow)::text), '' ORDER BY id)) FROM series_configuration"
}
wait_for() {   # what, url, the status codes that count, a container whose log helps
  attempts=0
  until case " $3 " in *" $(curl -s -o /dev/null -w '%{http_code}' "$2" || true) "*) true ;; *) false ;; esac; do
    attempts=$((attempts + 1))
    if [ "$attempts" -ge 60 ]; then
      docker logs --tail 30 "$4" >&2 || true
      echo "$1 didn't answer $3 on $2 within 2 minutes" >&2
      return 1
    fi
    sleep 2
  done
}
check_hour() {
  now=$(date -u +%H%M)
  if [ "$now" -ge 2155 ] && [ "$now" -le 2259 ]; then
    echo "Not between 21:55 and 23:00 (UTC): election's cron would start scraping" >&2
    return 1
  fi
}
dump_dir=$(mktemp -d)
stage=preparing
way_back() {
  echo "Fix the cause and run this block again, or go back to the old stack, on the commit before election-service:" >&2
  echo "  docker tag macro-frontend:before-step5 macro-frontend:latest; docker rm -f macrohub-macro-frontend macrohub-election" >&2
  echo '  git switch --detach "$(git log --diff-filter=A --format=%H -1 -- macro-backend/election-service/pom.xml)~1"' >&2
  echo "  docker compose --env-file $env_file -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --no-build --no-deps macro-analyzer macro-frontend" >&2
}
finish() {
  rc=$?
  rm -rf "$dump_dir"
  if [ "$rc" -eq 0 ]; then echo "RESTORE OK"; exit 0; fi
  echo "RESTORE FAILED (exit $rc). See the output above." >&2
  case "$stage:$(docker container inspect -f '{{.State.Running}}' macrohub-macro-analyzer 2>/dev/null || true)" in
    preparing:true)
      echo "Nothing was stopped: the old stack still runs. Fix the cause and run this block again." >&2 ;;
    preparing:)
      echo "This run changed nothing." >&2 ;;
    preparing:*)
      echo "This run stopped nothing, but an earlier run left macrohub-macro-analyzer stopped." >&2; way_back ;;
    restoring:*)
      echo "macroanalyzer is unchanged." >&2; way_back ;;
    switching:*)
      echo "The checksums matched, so election holds the data, and macroanalyzer is unchanged. Starting the new containers failed." >&2; way_back ;;
  esac
  exit "$rc"
}
trap finish EXIT

check_hour
[ -d macro-backend/election-service ] || { echo "Run this from the repo root of a clone that has election-service" >&2; exit 1; }
[ -f "$env_file" ] || { echo "$env_file is missing" >&2; exit 1; }
docker container inspect macrohub-macro-analyzer > /dev/null || { echo "macrohub-macro-analyzer is gone, so this has already run: election is the live data now" >&2; exit 1; }

# Build first, while nothing has stopped. Keep the old frontend's image: the build replaces its tag
docker image inspect macro-frontend:before-step5 > /dev/null 2>&1 || docker tag macro-frontend:latest macro-frontend:before-step5
(cd macro-backend && sh ./mvnw clean package && docker build -t regional-statistics-service regional-statistics-service && docker build -t macro-series-service macro-series-service && docker build -t parliament-service parliament-service && docker build -t election-service election-service)
dc build macro-frontend
check_hour

# The old tables stop changing: the monolith's cron is what writes them. Election's routes answer 502 from here until the end
stage=restoring
docker stop macrohub-macro-analyzer

# An existing volume has no election database (add -e ELECTION_DB_PASSWORD=... after -i if .env sets it)
docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh

# Flyway creates the tables and the config rows. Stop the service again before touching them
dc up -d --no-deps election
wait_for election-service http://localhost:8085/macro-analyzer/municipalities/elections "200 204" macrohub-election
docker stop macrohub-election

docker exec macrohub-postgres pg_dump -U macrouser -Fc --data-only --strict-names -t local_election_area -t local_election_party_result -t local_governance macroanalyzer > "$dump_dir/election.dump"
docker exec macrohub-postgres psql -v ON_ERROR_STOP=1 -U election -d election -c "TRUNCATE ${tables// /, }"
docker exec -i macrohub-postgres pg_restore -U election --no-owner --exit-on-error --single-transaction -d election < "$dump_dir/election.dump"

# All three tables and the config rows must match, or nothing is switched
checksums macrouser macroanalyzer > "$dump_dir/old.txt"
checksums election election > "$dump_dir/new.txt"
diff "$dump_dir/old.txt" "$dump_dir/new.txt"
cat "$dump_dir/new.txt"

stage=switching
docker start macrohub-election
wait_for election-service http://localhost:8085/macro-analyzer/municipalities/elections 200 macrohub-election
dc up -d --no-deps macro-frontend
wait_for "the gateway" http://localhost:8081/macro-analyzer/municipalities/elections 200 macrohub-macro-frontend
wait_for "the gateway's 404" http://localhost:8081/macro-analyzer/unknown 404 macrohub-macro-frontend
dc up -d --no-deps regional-statistics macro-series parliament
wait_for regional-statistics-service http://localhost:8082/macro-analyzer/regional-statistics/indicators 200 macrohub-regional-statistics
wait_for macro-series-service http://localhost:8083/macro-analyzer/series-config 200 macrohub-macro-series
wait_for parliament-service http://localhost:8084/macro-analyzer/parliament/parties 200 macrohub-parliament

docker rm macrohub-macro-analyzer
)
```

A failed `diff` prints the rows that differ and exits there. Compose warns about the orphan `macrohub-macro-analyzer` while the block runs, which is expected, so don't pass `--remove-orphans`. After `RESTORE OK`, open the pages and check them. If they are wrong, go back as under step 5's "Rollback", which works until `macroanalyzer` is dropped. `macro-analyzer:latest` and `macro-frontend:before-step5` stay as images until you `docker rmi` them.

#### Dropping the old database

**A separate step, for the owner, after checking that the pages work.** Nothing reads `macroanalyzer` after "Restoring election", and it holds only the three copied tables and the history of the old migrations. This can't be undone, other than by re-scraping election. It is one command, and it needs no restart:

```bash
docker exec macrohub-postgres psql -v ON_ERROR_STOP=1 -U macrouser -d postgres -c 'DROP DATABASE macroanalyzer'
```

Don't delete the volume instead: it holds `parliament`, the only copy of its data.

The running `macrohub-postgres` still has `POSTGRES_DB=macroanalyzer` in its environment, and its healthcheck names that database. `pg_isready` doesn't log in, so the container stays healthy, but Postgres then logs `FATAL: database "macroanalyzer" does not exist` every 10 seconds. To stop that, re-create the container at a quiet moment. The services answer 500 for a few seconds and reconnect by themselves; only images built before the connection pool also need a `docker restart` of the four services afterwards:

```bash
docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --wait postgres
```

### Code

- [ ] Create the service's Maven module, with its own `*Application` in the package `com.kg.macroanalyzer`. `@SpringBootApplication` scans from its own package, so that is what finds the `macro-platform` beans. Set `spring-boot.run.skip` to false, and add it to `<modules>` in the parent pom.
- [ ] Don't let its base `application.properties` default to the cluster or port 31274. Default to the Compose Postgres (`localhost:5432/<db>`) with the service's role, and copy `BaseConfigurationTest`, which pins that. It needs no `dev` or `prod` profile, and no `.env` import unless it has keys.
- [ ] Set `spring.jooq.sql-dialect=POSTGRES` and the three `spring.datasource.hikari.*` lines (`maximum-pool-size`, `minimum-idle`, `connection-timeout`) in its `application.properties`, as the other services do. Without them it still starts, on Hikari's default of 10 connections, all kept open, which breaks the sum under "Database" in `CLAUDE.md`. `ConnectionPoolIntegrationTest` pins them.
- [ ] Move the module's code. Remove it from the monolith.
- [ ] Generate a jOOQ model per service from its own database, in the package `com.kg.macroanalyzer.<module>.jooq.generated`. Give it a profile named after the service (`jooq-codegen-<service>`) that sets `jooq.codegen.url`, `.user` and `.password` to the Compose Postgres: the parent pom has no default. `macro-platform`'s profile has its own name too, so the `-am` a service's build needs doesn't run it.
- [ ] Move the module's tests and fixtures. Give the service its own `ArchitectureTest` for its layers, a copy of `NoStaticResourceHandlersHttpTest`, `ManualScrapePathsHttpTest` and `ConnectionPoolIntegrationTest`, and an application test like `RegionalStatisticsApplicationTests`, which pins both Flyway runs.
- [ ] Update the monolith's `ArchitectureTest` (`MODULES`, `DOMAIN_MODULES`).
- [ ] Update `MonolithSeriesConfigIntegrationTest`: `shouldRouteEveryConfig_toItsDomainsScrapeFlow` lists the configs still in the monolith by name (three after step 4), so each service takes its own entries.

### Manual scrape endpoints

Every service exposes the same three endpoints, where `<service>` is `macro-series`, `regional-statistics`, `parliament` or `election`:

- `POST /macro-analyzer/scrape/<service>/enqueue?name=`
- `POST /macro-analyzer/scrape/<service>/enqueue-all`
- `POST /macro-analyzer/scrape/<service>/scrape-from-queue`

Each service gets one gateway route, `/macro-analyzer/scrape/<service>/`, so nginx needs no map of series names to services. The endpoints stay public, per [ADR 0004](adrs/0004-keep-manual-scrape-endpoints-public-for-now.md). The old `/macro-analyzer/enqueue`, `/enqueue-all` and `/scrape-from-queue` went away in step 5. `macro-backend/macro-infra/scripts/scrape-all.sh`, which replaces the global `enqueue-all`, loops over the four names. It treats 409 as fine, because the service's cron may already have started on the queue it just filled.

- [ ] Set the service's path prefix to `macro-analyzer/scrape/<service>` (`macro.scrape.path-prefix`). It has no default, so a service without it fails at start-up with `Could not resolve placeholder`.
- [ ] Add the route in nginx, with the same 30-minute `proxy_read_timeout` as the other routes, and in the Vite proxy.

### Cut over

- [ ] Delete the domain's `series_configuration` rows from the monolith with a migration, **in the same PR that removes its flows**. `ScrapeService` refuses to start if a config names a flow that isn't registered.
- [ ] Delete the domain's `scrape_action_queue` rows from the monolith in the same migration. An item whose name matches no config stays pending, and fails with a warning on every cron run.
- [ ] Switch the gateway routes to the new service, in nginx and in the Vite proxy.
- [ ] Add the service to the enqueue-all replacement script.
- [ ] After verifying the service locally, drop the old tables with a monolith migration and regenerate the monolith's jOOQ model. Do this in a separate small PR, after the owner's stack has been verified. The monolith's code no longer reads them at that point. Drop a trigger's function in the same migration if nothing else uses it (`pg_trigger`; jOOQ's model doesn't show it). First compare each table in the two local databases, by checksum or row count, or by its distinct keys if the service has removed duplicates: the drop deletes the local copy, and not everything can be re-scraped (see parliament's activity under "Data"). Generate from a throwaway database at the new migration (`-Djooq.codegen.url=...`): the default, the Compose Postgres, only gets there once its monolith has restarted on the new code, and generating from it before that changes nothing. The model's diff should only delete those tables and the references to them (and `Sequences.java` or `Indexes.java`, if it has no sequence or index left), and `macro-platform`'s model, which the same command regenerates, should not change.
- [ ] Add the service to `docker-compose-all.yml`, with its image, database URL and role. CI needs no change: the `backend` job runs every module of the reactor. Add the image to the after-merge commands in `.claude/skills/ship/SKILL.md`.
- [ ] Update `CLAUDE.md`, `macro-backend/ARCHITECTURE.md`, the service's own README and this guide.

Note that `EnqueueService.enqueue` ignores unknown names: `POST .../enqueue?name=<another service's series>` answers 202 and does nothing.

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
- A stack that upgrades without `--no-deps` re-creates `macrohub-postgres`, on the same volume, because the service's password variable is new in its environment. That is harmless, but it restarts every service's connections (see step 5's differences, and the note there about the connection pool that came later).
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

- [x] Run the extraction checklist for election, with the new database `election`. Skip the cut-over items that delete config rows, delete queue rows and drop old tables with a monolith migration: the monolith itself becomes election, so they have no target. The old tables go away with the `macroanalyzer` database.
- [x] Rename the `macro-analyzer` module, application class, image, jar and Compose service to election.
- [x] Delete the monolith's migrations (V1 to V38) and the old jOOQ model from the tree. They stay in git history.
- [x] Route `/macro-analyzer/scrape/election/` to election. Its new prefix removes the old `/macro-analyzer/enqueue`, `/enqueue-all` and `/scrape-from-queue`, so remove the monolith's call from the script, which now only loops over the four names. Every service now sets its own prefix, so remove the `macro-analyzer` default.
- [x] Change the catch-all `location /macro-analyzer/` in nginx to answer 404. Without it, unknown API paths would fall through to `location /` and get `index.html` with a 200.
- [x] Remove `BACKEND_URL`, the `macroanalyzer` database from `POSTGRES_DB`, and the `macrouser` settings no service uses.
- [x] Rewrite the architecture and build sections of `CLAUDE.md`, and retire or rewrite `macro-backend/macro-analyzer/ARCHITECTURE.md`.

**Verify:** the checklist's verify list, plus: on a clean checkout with empty volumes (a throwaway project, as under "Verify", not the volume that holds `parliament`), `docker-compose-all.yml` starts all four services, the script fills them and every page works.

**Rollback:** until "Dropping the old database" has run, `macroanalyzer` is untouched, and the old monolith can come back on it, also after `RESTORE OK` has removed its container. From the repo root of the clone:

```bash
docker tag macro-frontend:before-step5 macro-frontend:latest
docker rm -f macrohub-macro-frontend macrohub-election
git switch --detach "$(git log --diff-filter=A --format=%H -1 -- macro-backend/election-service/pom.xml)~1"
docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --no-build --no-deps macro-analyzer macro-frontend
```

The old commit's Compose file defines `macro-analyzer`, and `--no-deps` keeps `macrohub-postgres` as it is. Those are the block's last printed lines, for every stage. The old monolith serves the data as it was when the block ran, so anything scraped into `election` since then isn't there. Then revert the PR, and drop `election` once nothing needs it. The other three services keep running on the new images, which answer the same.

After `macroanalyzer` has been dropped, revert the PR, re-create `macroanalyzer`, which the monolith migrates to V38 on its next start, and re-scrape election. Keep the volume and the other services' databases: `parliament` is the only copy of its data.

**What differed:**

The template held, with these details. The checklist above has been corrected where this step changes it.

- The module is `election-service`, on port 8085 inside and outside the container. The database and role are `election`, the Compose service `election`, the image `election-service` and the jOOQ profile `jooq-codegen-election`. The Java base package and the URL prefix `/macro-analyzer` stay. No frontend API changed. Port 8080 is free now.
- The baseline is `pg_dump` output laid out by hand. `pg_dump --schema-only` of the three tables, and of `series_configuration` and `scrape_action_queue`, in `election` is identical to `macroanalyzer`'s apart from the owner, and the three config rows have the same checksum, with `id` and `global_id` as literals (113 to 115, sequence set to 115). The election tables have natural keys and no sequence of their own. `local_governance` is empty. Unlike the earlier steps, nothing here is deleted from a monolith by migration: the whole `db/migration` folder went.
- **`macro-platform`'s model is generated from `election`**, by the profile `jooq-codegen-platform`, with `includes` for its three tables. From a throwaway database with both Flyway runs applied, it reproduced the committed model with no content diff, and neither `flyway_platform_schema_history` nor `election`'s tables get into it. The profile is named so that a service's `-am` doesn't run it, and `mvn -P jooq-codegen compile` no longer exists.
- **The parent pom's `jooq.codegen.url`, `.user` and `.password` are gone.** They meant "the monolith's database", and now every codegen profile sets its own. A profile can't then fall through to a default.
- **`macro.scrape.path-prefix` has no default**, so a service without one fails at start-up with `Could not resolve placeholder 'macro.scrape.path-prefix'` (pinned by `PathPrefixRequiredTest`). **`macro.platform.flyway.enabled` is gone**, and the platform run always happens. A service without `baseline-on-migrate` fails at start-up with "Found non-empty schema(s)". The three existing services' properties lost that line, and the file in their `db/platform/V1` keeps a comment naming it and the monolith: **editing it, even a comment, changes Flyway's checksum and would fail validation on every existing database.** The same goes for the services' `V1` comments.
- **Postgres:** `POSTGRES_DB` is now `postgres`. Unset, the image would create a database named after `POSTGRES_USER`. **`POSTGRES_USER` stays `macrouser`, with a comment that says what it is**, because renaming it isn't safe: an existing volume has that role, and ignores the new name, so after the container is re-created, the init script (`psql --username "$POSTGRES_USER"`) would fail. No service logs in with it. An empty volume gets exactly the four service databases plus `postgres`.
- **Found while testing: a re-created Postgres leaves every service answering 500 until it is restarted.** `JDBCConfiguration` opens one connection at start-up, with `DriverManager`, and holds it for the application's lifetime, despite Hikari being there for Flyway. So the "harmless, but it restarts every service's connections" under step 3's differences understated it. This was already the case, and isn't changed here (a real connection pool is a change of its own, and one for every service): the guide and `CLAUDE.md` now say so, and `--no-deps` matters more than it looked. *2026-11-02: fixed after step 5. Every service now uses Boot's pool and `DSLContext` and reconnects by itself: "Database" in `CLAUDE.md`.*
- After `DROP DATABASE macroanalyzer`, the old container's healthcheck stays healthy, because `pg_isready` doesn't log in, but Postgres logs a `FATAL` every 10 seconds until the container is re-created with the new `POSTGRES_DB`: see "Dropping the old database".
- Rules kept: `ArchitectureTest`'s checks of `macro-platform` moved to its own new `ArchitectureTest`, which sees its classes directly and has `importIncludesMacroPlatform`. Tried by adding a class outside the modules, `shared` using `scrape`, and `scrape.domain` using an adaptor: all 4 rules failed as they should. It is a little stricter than before: `shared` may only use itself and the platform's model, and `scrape` itself, `shared` and that model. `macro-platform` needed `archunit-junit5` as a test dependency. `NoStaticResourceHandlersHttpTest` is in all four services, and so is `spring.web.resources.add-mappings=false`.
- Tests went from 404 to 415: `macro-platform` 31 to 39, the election module 74 (as `macro-analyzer`) to 77, and the other three unchanged at 63, 165 and 71. By name, 9 went and 16 came. `MacroAnalyzerApplicationTests`, `MonolithSeriesConfigIntegrationTest` (both tests) and the parameterized `BaseConfigurationTest` (the second case read the deleted `dev` file) became `ElectionApplicationTests` and a `BaseConfigurationTest` like the other services'. `MonolithPlatformDefaultsHttpTest` (both tests) described the defaults this step removes, and `ManualScrapePathsHttpTest`, `PathPrefixRequiredTest` and the two Flyway tests in `ElectionApplicationTests` replace it. The monolith's two multi-module `ArchitectureTest` rules became the rules `moduleOnlyDependsOnItselfAndThePlatformCore`, `platformDoesNotDependOnTheModule` and `onlyPostgresAdaptorsUseTheJooqModel`, which the other services already have. `SeriesConfigRepositoryTest` and the Valmyndigheten tests moved as they were. `application-test.properties` moved to `src/test/resources`, on `postgres:16` rather than 15.2.
- `macro-analyzer/ARCHITECTURE.md` moved to `macro-backend/ARCHITECTURE.md`, rewritten for four services. Its README became `election-service/README.md`, and the root README got the sources it listed and current start-up steps.
- `k8s/java/deployment.yaml` got a comment that it is out of date and not deployable, and `k8s/README.md` lost its instructions to build and run the deleted image. Step 6 lists this.
- Beyond the plan: `location = /macro-analyzer` also answers 404, because `/macro-analyzer` without the slash would otherwise get `index.html`. With `return` rather than `proxy_pass`, nginx no longer redirects it to the slash. The Vite proxy does the same with a `bypass` that returns false, so `npm run dev` also answers 404 on unknown API paths.

What was tried, on throwaway Compose projects:

- **A clean checkout with empty volumes:** `docker-compose-all.yml` started all four services and the frontend, election loaded the 11 party colours as soon as parliament answered, and no log had a failed flow registration. Each of the four roles is refused on the other three databases, `postgres` and `template1`, and `election` also on `macroanalyzer` on a restored copy.
- **`scrape-all.sh`, for real, once, with the owner's keys:** it exited 0 after 7 min 31 s, with 0 `ERROR` log lines and no failed item. Per service, in parallel: macro-series 51 s (108 series), election 2 min 7 s (3 configs), parliament 5 min 28 s (2 configs), regional-statistics 7 min 30 s. So the macro-series estimate above was right. All went through the gateway, so the 30-minute timeout held on three scrape routes past nginx's default of 60 s; `nginx -T` shows the one setting, on the `server`, and no route overrides it.
- **Row counts against the running stack:** `election` the same (339 areas, 6,733 party rows, 0 governance, and the same `source_md5` on every area), parliament the same (3,376 members and so on), regional-statistics the same (2,401,042 values and so on). In macro-series, `fed_series` has 33,407 rows against 33,416 (FRED's rolling 10-year window, see step 3) and `policy_rate_sweden` 8,144 against 8,143 (a newer date). The queues differ, as a new database has no old queue rows.
- **Through the new gateway, against the running stack on `http://localhost:8081`, on a restored copy:** all 693 election requests (every kommun, region and valkrets, with and without `?year=`, and the unknown cases, 6.7 MB) were byte for byte the same, except the two `/municipalities/elections` lists, which differ only in the order of parties with equal votes. So were all 47 others sampled: `/series-config`, `/indicators` and 6 parliament requests, 18 `/chart-data` requests, 11 `/regional-statistics/values` requests and 10 member details. On the database filled by the scrape, the 693 also match apart from that order. Of the 47, only three differ, for what the sources have changed since the running stack last scraped: two members' status, in two lists, and that new `policy_rate_sweden` point.
- **Gateway:** unknown API paths, `/macro-analyzer` itself, the old `/macro-analyzer/enqueue`, `/enqueue-all` and `/scrape-from-queue` and an unknown `/scrape/` name answer 404 for GET, POST and OPTIONS, with and without an `Origin` header. `/` and the SPA routes return `index.html` with a 200. A second `scrape-from-queue` during a run answered 409.
- **Party colours:** with `parliament` stopped, its routes answered 502 and election kept answering in 0.07 s with the colours held (SD `#FFCC00`). After restarting election with parliament still stopped, it answered with Valmyndigheten's (SD `#4E83A3`). After starting parliament they were back within 22 s.
- **"Restoring election" was run as written**, with only the names and ports swapped, from a copy of the running stack's databases and the old images, including the real builds: `RESTORE OK`, all three tables and the config rows matched, a 280 KB dump, and `macrohub-postgres`' stand-in wasn't re-created. **A real election scrape on the restored copy said `Fetching 0 of 290`, `0 of 20` and `0 of 1` files, in 0.45 s, so the md5 state came across.**
- Its failures were tried the same way. During the hour (a fake `date`): refused before anything else. A failed build: nothing stopped, and the old stack kept answering. A wrong `ELECTION_DB_PASSWORD`: `RESTORE FAILED` with the service's log after 2 minutes. A checksum altered on purpose: the differing row printed, and the gateway not switched. In these, `macroanalyzer` stayed at V38 with its 339 rows, no dump was left behind, and the printed commands brought the old monolith back. Running the block again after the altered checksum ended in `RESTORE OK`. A failure after the new frontend had started (the last stage): the printed commands brought back the old gateway and monolith, with `/enqueue` answering. So they did after the wrong password, and after a re-run that failed before stopping anything. **The same commands were also run after a real `RESTORE OK` with its builds, once the old container had gone:** Compose re-created `macro-analyzer` from its image, on V38 with no migration, `macrohub-postgres`' stand-in wasn't re-created, the colours loaded from `parliament`, and all 348 election requests, every area and the unknown cases, were byte for byte the running stack's. Once the old container is gone, the block refuses before stopping anything, and says this run changed nothing. That preflight first used `docker inspect`, which also matches images, so it became `docker container inspect`.
- `DROP DATABASE macroanalyzer` on the copy: the stack kept answering, the init script could be re-run, and re-creating Postgres from the new Compose file kept the data, with the services needing the restart described above (no longer needed since the connection pool of 2026-11-02).
- `npm run dev`: only the backends' ports replaced, to the throwaway stack: all reads and the POST on all four `/scrape/` routes reached the right service (seen in its log), and unknown paths answered 404. `npm run typecheck`, `build` and `lint` pass.
- **No page was opened in a browser.**

## Step 6: First real Kubernetes deployment

**Goal:** the app runs in a cluster for the first time. `macro-backend/macro-infra/k8s/README.md` is the procedure.

**Tasks:**

- [x] Replace the existing manifests in `macro-backend/macro-infra/k8s/`. `java/`, `ingress/` and `kcat/` are gone, and `postgres/` describes the new Postgres 16, with no plain-text password.
- [x] Add manifests for all four services, with database settings and API keys, and `ELECTION_PARLIAMENT_URL` on election. Secrets are Kubernetes Secrets made by `create-secrets.sh`, from the env file and generated values. No value is in the repo.
- [x] Add routing: a frontend manifest with nginx, on NodePort 30081 ([ADR 0006](adrs/0006-kubernetes-as-eventual-deployment-target.md)).
- [x] Create the Postgres databases and roles: the same init script as Compose, through a ConfigMap on an empty volume. `deploy.sh` also runs it on a retained volume, where it keeps the roles' passwords the same as the Secret's.
- [x] Set up image delivery: `build-images.sh`, with `imagePullPolicy: Never`, tagged with the commit, and no registry.
- [x] Revisit [ADR 0004](adrs/0004-keep-manual-scrape-endpoints-public-for-now.md). The decision stands for now, and the owner's question about exposure is open.
- [x] Backups per database: the CronJob `postgres-backup`, with a tested restore.
- [x] Memory requests and limits, from the first scrape on the cluster: see below.
- [x] Kafka and Zookeeper are gone from `kubernetes_setup.sh`, with helm, which only they used. No application code uses Kafka.

**Rollback:** `k8s/teardown.sh` removes the deployment and keeps the volumes. Nothing depends on it, and Compose's databases stay the source of truth.

**What differed:**

- **A new namespace and a new Postgres,** `macro-hub`, rather than the old manifests' namespace. The owner's `macro-analyzer` namespace, its Postgres 13, volume `postgres-pv` and NodePort 31274 are untouched and no longer described by these files.
- **`microk8s images import` rather than `microk8s ctr image import`,** because here `microk8s ctr` runs through sudo, which needs a password. `images import` only needs the `microk8s` group. `deploy.sh` checks that the tag has been imported with the snap's `ctr` binary directly, which the group can also use, on containerd's socket.
- **`build-images.sh` builds from `git archive HEAD`,** so a tag says exactly what is in the image, in a non-hidden directory under `$HOME`, as Docker as a snap can't read `/tmp` or hidden directories.
- **The passwords live outside the repo in `~/.config/macro-hub/k8s-db-passwords.env`,** because a retained volume keeps its roles' passwords after the Secrets are gone. Generating new ones on every run would lock the services out.
- **`deploy.sh` re-applies the roles and clears a Released volume's old claim uid.** `claimRef` keeps the claim's name, so only the same claim can bind it again.
- **`enableServiceLinks: false`,** beyond the plan, because Kubernetes would otherwise put `<SERVICE>_PORT` and `POSTGRES_PORT` variables in the pods, next to the ones Spring and the image read. **`-XX:+ExitOnOutOfMemoryError`** is also beyond the plan, so a heap that runs out shows as a restart.
- **`/actuator/health/{liveness,readiness}` don't check the database,** on purpose, and no application code changed. `/actuator` isn't on the gateway: it gets `index.html` like any SPA route.
- **The metrics addon stays off,** so peaks come from each pod's `/sys/fs/cgroup/memory.peak`, also sampled every 10 s, and `kubectl top` isn't available.

Memory in the first full scrape (`-XX:MaxRAMPercentage=70` everywhere). A JVM grows its heap before it collects, so a peak near a limit's heap is not proof that it needed that much. Against ADR 0001's estimate of 1.5 to 2 GiB for four JVMs: 1.5 GiB idle, as all four rest near 390 MiB, and 3.1 GiB if all four peak together.

| Pod | Limit during the scrape | Peak | Own memory at its peak | Limit now | Request |
| --- | --- | --- | --- | --- | --- |
| regional-statistics | 1536 MiB | 1348 MiB | 1341 MiB | 1536 MiB | 768 MiB |
| parliament | 1 GiB | 932 MiB | 926 MiB | 1280 MiB | 512 MiB |
| election | 1 GiB | 467 MiB | 462 MiB | 768 MiB | 384 MiB |
| macro-series | 1 GiB | 389 MiB | 383 MiB | 768 MiB | 384 MiB |
| postgres | 2 GiB | 1347 MiB, mostly page cache | 55 MiB | 2 GiB | 512 MiB |
| frontend | 256 MiB | 17 MiB | 12 MiB | 128 MiB | 32 MiB |

The limits now have only had a re-deployment and small scrapes, not another full scrape, since that was to run once. The nightly cron is the next test of them.

What was tried, on the real cluster, in `macro-hub`, on 2026-11-02:

- **From a namespace that didn't exist:** the README's three commands brought everything up, in 1 min 5 s for `deploy.sh` after 1 min 24 s for the build. The one difference was passing the env file's path, as this ran in a git worktree.
- **Teardown and back:** `teardown.sh` left the namespace with no pods, Secrets or claims of its own and both volumes Released. `create-secrets.sh` and `deploy.sh` brought it back in 36 s, on the same volumes, with every table's row count unchanged and the dumps still there. There is no scrape in that.
- **The password change in the README:** done for real, all services back with no restarts. A partial first attempt, where `create-secrets.sh` was killed by the test's own `| head -1`, showed what a mismatch looks like: the services in `CrashLoopBackOff`, with `password authentication failed` in their logs, until the Secrets were right.
- **Roles:** each of the four roles connected to its own database and was refused on the other three, `postgres` and `template1`, over TCP inside the pod.
- **Postgres from outside:** a ClusterIP with no NodePort or hostPort. Another machine has no route to it. **This machine's own routes do reach it,** as kube-proxy handles ClusterIPs for local traffic, including from its Docker containers, so `pg_isready` from a container on this machine said "accepting connections". Each role still needs its password there. A NetworkPolicy wasn't part of this step.
- **The first full scrape, once, through the gateway with `scrape-all.sh`:** exit 0 after 8 min 5 s, with no `ERROR` log line, no failed item and no restart. Per service, in parallel: macro-series 55 s (108 series), election 2 min 3 s (3 configs), parliament 5 min 16 s (2 configs), regional-statistics 8 min 4 s. So the 30-minute timeout held past nginx's default, on the longest route, and `nginx -T` shows the one setting.
- **Row counts against Compose:** the same in every table of `regional_statistics` (2,401,042 values), `parliament` and `election`, apart from the queues, which hold a database's own history. In `macro_series`, newer points from the sources: `policy_rate_sweden` 8,144 against 8,143, `exchange_usd_sek` 8,497 against 8,496, two Japanese bond series +1 each, and `fed_series` 33,406 against 33,416 (FRED's rolling 10-year window, see step 3).
- **Through the gateway, against Compose on `http://localhost:8081`, 512 requests at a low rate:** all 348 election requests (every kommun, region and valkrets, unknown codes included, with the colours) and all 85 `regional-statistics` requests are the same, ignoring the order of parties with equal votes. Of 55 macro-series requests, 4 differ, all by being longer: the same newer points. Of 24 parliament requests, 3 differ: two members' status, in two lists, and one member's new motion. Riksdagen has changed since Compose last scraped.
- **Gateway:** unknown API paths, `/macro-analyzer` itself, the old `/enqueue-all` and an unknown `/scrape/` name answer 404 for GET, POST and OPTIONS. `/` and SPA routes answer the same `index.html` as Compose's, with a 200. A manual `enqueue?name=` and `scrape-from-queue` on macro-series answered 202 and 200.
- **Postgres killed while services ran, twice:** the pod deleted, and an immediate stop inside it, which restarted the container and ran crash recovery. Each time the services didn't restart. The only failures seen were a 500 on one route for 1 to 2 s.
- **A service pod killed:** macro-series answered again after 12 s. **Colours:** with parliament scaled to 0, its routes answered 502 and election kept answering in 0.06 s with the colours held (SD `#FFCC00`). Election restarted with parliament down answered with Valmyndigheten's (SD `#4E83A3`), and 13 s after scaling parliament up the colours were back, over its Service name.
- **Backups:** the CronJob, triggered by hand, wrote four dumps: regional_statistics 16 MB, macro_series 13 MB, parliament 2.6 MB, election 272 KB. It keeps 7 per database, tried with old-named files, and a run with a wrong password failed the job, removed nothing and left no partial file. **Both restores in the README were run:** `regional_statistics` into a scratch database in 10 s, with 2,401,042 rows, and `election` replacing the real database, after which its role's rights were the same as before, Flyway said "up to date", and `/municipalities/elections` was byte for byte the same.
- **Not tried:** no page was opened in a browser, as none is available here. The 03:00 and 22:00 UTC schedules haven't fired yet, and neither has an OOM kill. A node failure isn't tried either: with one node, Postgres and the services go down together, so the "doesn't cover" case under "Database" in `CLAUDE.md` is unlikely, and a liveness probe wouldn't solve it, because the process still answers.

**Nightly behaviour to expect:** every service's cron enqueues in the 22:00 UTC hour, so the cluster and Compose now both scrape, from one machine, with the same keys and at the same time. Macro-series fetches its series and parliament its members again every night, so the same API keys and sources see both runs. Election and SCB skip what hasn't changed. When SCB's tables change, both fetch them, each within SCB's 30 calls per 10 s but not together, so its retries may do more work on those nights. Whether a source minds this hasn't been seen yet: the first night will tell, in the logs' `FAILED` counts.
