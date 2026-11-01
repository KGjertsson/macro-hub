# Microservice migration guide

This is the living plan for splitting the backend monolith into four services: macro-series, regional-statistics, parliament and election. The decisions behind it are in [`docs/adrs/`](adrs/README.md), and the [design page](adrs/microservice-split-design.html) has the diagrams. If this guide and an ADR disagree, the ADR wins.

Statements about the current code were checked against commit `07c7482`. Anything marked **Suggested** or **Open** is not decided.

## Keeping this guide updated

In the same PR as each step:

1. Tick the tasks you finished and update the status table.
2. Record what differed from the plan under "What differed" at the end of the step, even if the answer is "nothing".
3. Move answered open questions into the step they affect. If the answer is a larger decision, add an ADR.
4. Update `CLAUDE.md` so it describes what is now true.

## Starting point

Nothing is deployed anywhere. Local development uses Docker Compose, and Kubernetes is the eventual deployment target ([ADR 0006](adrs/0006-kubernetes-as-eventual-deployment-target.md)). There is no production data, downtime or live traffic to protect, and today's shared database only exists on development machines. So:

- A new database starts empty and is **re-scraped by default**. Dump and restore is only a time-saver for a local database.
- **Rollback for any step means reverting its PR and re-creating the local databases.** Re-creating them matters: Flyway refuses to start when the database has an applied migration that is missing from the code, which is what a reverted migration looks like.
- `docker-compose-all.yml` runs all services at each stage. The k8s files only need to keep working as far as they do today until step 6.
- Steps are still one PR each, because that keeps `main` working and each change reviewable.

## Status

| Step | Description | Status |
| --- | --- | --- |
| 0 | Record the decisions and start this guide | Done when this PR merges |
| 1 | Prepare inside the monolith | Not started |
| 2 | Extract regional-statistics | Not started |
| 3 | Extract macro-series | Not started |
| 4 | Extract parliament, switch election to REST | Not started |
| 5 | Retire the monolith | Not started |
| 6 | First real Kubernetes deployment | Not started |

## Open questions

| Question | What is known | Needed by |
| --- | --- | --- |
| What are the exact per-service manual trigger paths? | They must differ per service, because nginx would need a map of every series name to its service to route `/enqueue?name=`. Election has three URL prefixes and macro-series has none below a common prefix, so the existing prefixes alone don't give every service a home. `InsertionAdaptor` moves to `macro-platform` with fixed paths today, so the prefix has to become configurable. | Step 2 |
| How do `macro-platform` and service migrations stay ordered? | See "Flyway" in the checklist. A version range prevents collisions, but not ordering problems. | Step 2 |
| Which is the gateway in the cluster: nginx in the frontend image, or Ingress routes? | `k8s/` has no frontend manifest. Compose keeps using nginx either way. With nginx, `BACKEND_URL`-style values need fully qualified service names. | Step 6 |
| How do images reach the cluster? | `k8s/java/deployment.yaml` uses `imagePullPolicy: Never`, with images imported by hand. | Step 6 |

## Extraction checklist

Steps 2 to 5 each use this checklist. `<service>` is the service's name, and `<db>` its database (`regional_statistics`, `macro_series`, `parliament`, `election`).

### Database and role

- [ ] Create the database and role:

  ```sql
  CREATE ROLE <db> LOGIN PASSWORD '...';
  CREATE DATABASE <db> OWNER <db>;
  REVOKE CONNECT ON DATABASE <db> FROM PUBLIC;
  ```

  The `REVOKE` matters, because every role may connect to a new database by default.
- [ ] Make this repeatable in Compose. Neither Compose file mounts an init script today. **Suggested:** mount one in both. **Postgres init scripts only run on an empty data volume**, so an existing volume needs an explicit step: run the SQL with `psql` against the running container, or delete the volume and re-scrape.

### Flyway

The monolith has no `spring.flyway.*` settings, so it uses the defaults: `classpath:db/migration` and `flyway_schema_history`.

- [ ] Write a squashed baseline in the new service with the domain's tables and seed data, taken from the current database (`pg_dump --schema-only -t ...` is a good start). V1 to V32 stay in the monolith until step 5, and in git history after that.
- [ ] Include the seed data that migrations added. Today that is `scb_indicator`, `riksdag_party`, `riksdag_election`, `riksdag_election_result` and `riksdag_organ`. `local_governance` starts empty.
- [ ] Copy the domain's `series_configuration` rows into the baseline, selected by `scrape_flow`. Keep `id` and `global_id` so `/series-config` keeps its order, and write `global_id` as literals so the baseline doesn't need the `uuid-ossp` extension (no column default uses it).
- [ ] Set the `series_configuration` and `scrape_action_queue` sequences past the copied ids.
- [ ] Apply `macro-platform`'s migrations for those two tables.
  - They must live outside `db/migration` (**Suggested:** `db/platform`), so the monolith doesn't pick them up from the jar and try to create tables it already has. Each service adds that folder to `spring.flyway.locations`.
  - They need a version range that can't collide with the service's own (**Suggested:** V1 to V99 for the platform, V100 and up for services).
  - **Open:** a range alone doesn't solve ordering. Flyway rejects a migration below the highest applied version, so a new platform `V3` fails on a service already at `V100`. The options are `spring.flyway.out-of-order=true`, or a separate Flyway run and history table for the platform.

### Data

Re-scraping is the default. Dump and restore only saves time on a local database.

| Service | Re-scrape time | Notes |
| --- | --- | --- |
| regional-statistics | About 12 minutes | 2.4 M rows and a few hundred MB of heap. |
| macro-series | Not measured | Needs the Riksbanken and Fed keys. |
| parliament | About 7 minutes for activity | Activity is only fetched for the latest two terms, so older activity in a local database isn't re-created. |
| election | About 1.5 minutes | About 310 zips. |

- [ ] Trigger the scrape through the service's manual path, or wait for its cron during the 22:00 hour.
- [ ] Skip the queue rows on a fresh start. If you keep a local database and it has pending rows, copy them by `dataset_name` (`status = 0`; all local rows are currently done).

Optional time-saver, after Flyway has created the tables:

```bash
pg_dump -Fc --data-only -t scb_indicator -t scb_area_value -t scb_availability -t scb_table_state macroanalyzer > rs.dump
psql -d regional_statistics -c 'TRUNCATE scb_indicator CASCADE'   # removes the baseline's seed rows
pg_restore --no-owner -d regional_statistics rs.dump
```

Try this on a throwaway database first. Check that foreign keys and sequences come through as expected.

### Code

- [ ] Create the service's Maven module, with its own `*Application` in the package `com.kg.macroanalyzer`. `@SpringBootApplication` scans from its own package, so that is what finds the `macro-platform` beans.
- [ ] Move the module's code. Remove it from the monolith.
- [ ] Generate a jOOQ model per service from its own database. It needs its own package: `Tables`, `Public` and `DefaultCatalog` would otherwise be duplicate classes next to `macro-platform`'s model. The `jooq-codegen` profile's URL and user change with it.
- [ ] Move the module's tests and fixtures. Give the service its own `ArchitectureTest` for its layers.
- [ ] Update the monolith's `ArchitectureTest` (`MODULES`, `DOMAIN_MODULES`).
- [ ] Update `ChartSeriesConfigIntegrationTest`: `shouldRouteEveryNonChartConfig_toItsDomainsScrapeFlow` lists all six non-chart configs by name, so each service takes its own entries.

### Cut over

- [ ] Delete the domain's `series_configuration` rows from the monolith with a migration, **in the same PR that removes its flows**. `ScrapeService` refuses to start if a config names a flow that isn't registered.
- [ ] Delete the domain's `scrape_action_queue` rows from the monolith in the same migration. An item whose name matches no config stays pending, and fails with a warning on every cron run.
- [ ] Switch the gateway routes to the new service, in nginx and in the Vite proxy.
- [ ] Add the service to the enqueue-all replacement script.
- [ ] After verifying the service locally, drop the old tables with a monolith migration and regenerate the monolith's jOOQ model.
- [ ] Add the service to `docker-compose-all.yml`, with its image, database URL and role, and update CI.
- [ ] Update `CLAUDE.md`, `macro-backend/macro-analyzer/ARCHITECTURE.md`, `macro-backend/macro-analyzer/README.md` and this guide.

Note that `EnqueueService.enqueue` ignores unknown names. After a cut-over, `POST /macro-analyzer/enqueue?name=<moved series>` on the monolith still answers 202 and does nothing.

### Verify

- [ ] `mvn test` passes for the service and the monolith, and CI is green.
- [ ] The full stack starts, and the monolith logs no failed flow registration.
- [ ] Every page that uses the domain works through the gateway, and the responses match what the monolith returned.
- [ ] The role can't connect to another service's database.
- [ ] A manual scrape on the new service's path works, and a second one during the run answers 409.

### Rollback

Revert the PR and re-create the local databases.

## Step 1: Prepare inside the monolith

**Goal:** the repo builds as a reactor and routes per domain, while everything still runs as one application.

**Tasks:**

- [ ] Convert to a Maven reactor. **Suggested:** the parent `pom.xml` and `mvnw` in `macro-backend/`, with the modules `macro-platform` and `macro-analyzer`.
- [ ] Extract `scrape` and `shared` into `macro-platform`, keeping their package names. `MacroAnalyzerApplication` stays in `com.kg.macroanalyzer`, so component scanning still finds them.
- [ ] Give `macro-platform` its own jOOQ model, in its own package, for `series_configuration`, `scrape_action_queue` and `flyway_schema_history` (`scrape`'s repositories and `FlywayCatalogVersion` use them). Today's generated model has one package and is committed in `src/main/java`.
- [ ] Adjust what depends on the Maven layout: `.github/workflows/ci.yml` (`working-directory`, `mvnw`), `macro-backend/macro-analyzer/Dockerfile` (`COPY target/...`), the image build commands in `CLAUDE.md` and `macro-backend/macro-infra/k8s/README.md`, and the other docs.
- [ ] Split `location /macro-analyzer/` in `macro-frontend/nginx/default.conf.template` into per-domain routes, all still pointing at the monolith:
  - `/macro-analyzer/series-config` and `/macro-analyzer/chart-data`
  - `/macro-analyzer/regional-statistics/`
  - `/macro-analyzer/parliament/`
  - `/macro-analyzer/municipalities/`, `/macro-analyzer/regions/` and `/macro-analyzer/constituencies/`
  - `/macro-analyzer/` stays as the catch-all, which also carries the manual endpoints for now
- [ ] Give each domain its own URL variable (**Suggested:** `MACRO_SERIES_URL` and so on), with a default in `macro-frontend/Dockerfile`. The nginx entrypoint only substitutes variables that are defined: an unset variable stays as the literal `${X}`, and one set to an empty string leaves `set $backend ;`, so nginx fails to start. Both are easy to hit in Compose. Keep the rule from `BACKEND_URL`: `scheme://host:port`, with no trailing slash. Keep the 30-minute `proxy_read_timeout` on every route.
- [ ] **Suggested:** add a Vite proxy for `/macro-analyzer` to the monolith, split the same way, make the dev frontend call its own origin and move Vite off 8080, which ends the port clash. `rootUrl` falls back to `http://localhost:8080` when `VITE_API_URL` is unset. A `.env.development` file isn't an option: the pre-commit hook and CI reject `.env.<anything>`. Change the fallback or set the variable in the `dev` script.
- [ ] Update the local start-up steps in `CLAUDE.md`, with Compose as the default route. Running a service with `mvnw` and Vite stays possible, for working on one service.

**Verify:** CI is green. The Compose stack and `npm run dev` both serve every page, and the manual endpoints still work through the gateway.

**Rollback:** revert the PR. This step changes no database.

**What differed:** not started.

## Step 2: Extract regional-statistics

**Goal:** regional-statistics runs as its own service with its own database. This step is the template for the others: it has no dependents and the largest runtime gain.

**Owns:** `scb_indicator`, `scb_area_value`, `scb_availability`, `scb_table_state`; flow `SCB_REGIONAL`; config `ScbRegionalStatistics`.

**Tasks:**

- [ ] Answer the open questions on manual trigger paths and migration ordering, and record the answers here.
- [ ] Make the manual trigger paths configurable per service in `macro-platform`.
- [ ] Add the first version of the script that replaces `enqueue-all`, and update the "fresh database" note in `CLAUDE.md`. Until step 5 it also calls the monolith.
- [ ] Add `macro-platform`'s migrations for the queue and config tables.
- [ ] Run the extraction checklist.
- [ ] Add the image and its service in `docker-compose-all.yml`, and point the frontend's route variable at it. Leave room for the first scrape's heap if a memory limit is added.
- [ ] Route `/macro-analyzer/regional-statistics/` to the service.

**Verify:** the checklist's verify list, plus: the Områdesstatistik page (`/omraden`) shows data for DeSO and RegSO, and a second scrape with unchanged tables takes about 30 seconds. A running SCB scrape no longer delays a scrape in the monolith.

**Rollback:** revert the PR and re-create the local databases.

**What differed:** not started.

## Step 3: Extract macro-series

**Goal:** macro-series runs as its own service. This is the largest move, but it should be mechanical with the template.

**Owns:** the 41 rate, bond, bill, exchange-rate, Fed and Eurostat tables; flow `MACRO_POINT`; 108 configs.

**Tasks:**

- [ ] Run the extraction checklist.
- [ ] Move the `RIKSBANKEN_PROD_SUBSCRIPTION_KEY` and `FED_PROD_SUBSCRIPTION_KEY` settings to this service only, in `docker-compose-all.yml` and the service's `.env.example`.
- [ ] Move `ChartSeriesConfigIntegrationTest` here, including its count of 108 and its category set. Move its checks on other flows to their owners.
- [ ] Route `/macro-analyzer/series-config` and `/macro-analyzer/chart-data` to the service. Add the image and its service in `docker-compose-all.yml`.

**Verify:** the checklist's verify list, plus: `/series-config` lists 108 entries in the same order, and charts for a Riksbanken, a Fed and a Eurostat series render.

**Rollback:** revert the PR and re-create the local databases.

**What differed:** not started.

## Step 4: Extract parliament, and switch election to REST

**Goal:** parliament runs as its own service, and election no longer depends on its code. See [ADR 0005](adrs/0005-cached-rest-between-services-and-no-message-broker.md). The election switch can be its own PR before the extraction, while election and parliament are still both in the monolith.

**Owns:** `swedish_members_of_parliament`, `swedish_member_activity`, `riksdag_party`, `riksdag_election`, `riksdag_election_result`, `riksdag_organ`; flows `MEMBER_OF_PARLIAMENT` and `MEMBER_ACTIVITY`; configs `MembersOfParliament` and `MembersOfParliamentActivity`.

**Tasks:**

- [ ] Give election its own out-port for party colours, and use it in `LocalElectionService` in place of `PartyDirectory`.
- [ ] Add the HTTP adaptor for `GET /macro-analyzer/parliament/parties`, with a configurable URL, a background refresh and the fallbacks from ADR 0005. It must accept the 204 the endpoint gives for an empty list, and must not block start-up. While both are in the monolith, the configurable URL points at the monolith itself (`http://localhost:8080`).
- [ ] Add a contract test that pins `code` and `color`, and update `LocalElectionServiceTest`.
- [ ] Remove the `election` entry from `DOMAIN_MODULE_EXTRA_DEPENDENCIES` in `ArchitectureTest`, and remove `PartyDirectory` from parliament.
- [ ] Run the extraction checklist for parliament.
- [ ] Route `/macro-analyzer/parliament/` to the service, and point election's URL at it. Add the image and its service in `docker-compose-all.yml`.

**Verify:** the checklist's verify list, plus: with parliament stopped, election pages still answer and keep Riksdag colours after a refresh fails. With parliament stopped and election just restarted, election pages still answer, with Valmyndigheten's colours.

**Rollback:** revert the PR and re-create the local databases.

**What differed:** not started.

## Step 5: Retire the monolith

**Goal:** what remains of the monolith is election, and it becomes a normal service. No trace of the monolith is left.

**Owns:** `local_election_area`, `local_election_party_result`, `local_governance`; flow `LOCAL_ELECTION`; configs `MunicipalElections`, `RegionElections` and `ParliamentElections`.

**Tasks:**

- [ ] Run the extraction checklist for election, with the new database `election`. Skip the cut-over items that delete config rows, delete queue rows and drop old tables with a monolith migration: the monolith itself becomes election, so they have no target. The old tables go away with the `macroanalyzer` database.
- [ ] Rename the `macro-analyzer` module, application class, image, jar and Compose service to election.
- [ ] Delete V1 to V32 and the old jOOQ model from the tree. They stay in git history.
- [ ] Add the `/macro-analyzer/enqueue`-style replacement for election, and remove the monolith's call from the script.
- [ ] Change the catch-all `location /macro-analyzer/` in nginx to answer 404. Without it, unknown API paths would fall through to `location /` and get `index.html` with a 200.
- [ ] Remove `BACKEND_URL`, the `macroanalyzer` database from `POSTGRES_DB`, and the `macrouser` settings no service uses.
- [ ] Rewrite the architecture and build sections of `CLAUDE.md`, and retire or rewrite `macro-backend/macro-analyzer/ARCHITECTURE.md`.

**Verify:** the checklist's verify list, plus: on a clean checkout with empty volumes, `docker-compose-all.yml` starts all four services, the script fills them and every page works.

**Rollback:** revert the PR and re-create the local databases.

**What differed:** not started.

## Step 6: First real Kubernetes deployment

**Goal:** the app runs in a cluster for the first time. This step is only an outline. It needs its own design when it starts.

It comes last here, but it doesn't depend on the split being finished, so it can move earlier. The k8s files aren't kept in step with the services before this, so expect them to need catching up.

**Tasks:**

- [ ] Fix or replace the existing manifests in `macro-backend/macro-infra/k8s/`. `java/service.yaml` selects a label the pods don't have, `ingress/test-ingress.yaml` names a Service that doesn't exist, and `postgres/postgres-deployment.yaml` uses `postgres:13` while Compose uses `postgres:16`.
- [ ] Add manifests for all four services, with database settings and API keys. **Open:** how secrets are stored. `postgres-configmap.yaml` holds the password in plain text today.
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
