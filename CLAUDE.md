# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

macro-hub visualizes macro-economic trends from multiple data sources (Riksbanken, Eurostat, Federal Reserve, Swedish Parliament, Valmyndigheten, SCB). It consists of:

- **macro-backend/** — Java 25 / Spring Boot 4.1.2 backend, a Maven reactor (parent `pom.xml` and `mvnw` here):
  - **macro-platform/** — library with the `scrape` core and `shared` (no application class; its only migrations are `db/platform`, which only services that opt in run)
  - **macro-analyzer/** — the monolith: the domain modules that haven't moved yet, `MacroAnalyzerApplication`, its Flyway migrations and properties files
  - **regional-statistics-service/** — the first extracted service: the `regionalstatistics` module, `RegionalStatisticsApplication`, its own migrations and properties files
  - **macro-series-service/** — the chart series: the `macroseries` module, `MacroSeriesApplication`, its own migrations and properties files, and the Riksbanken and Fed keys
  - **parliament-service/** — Riksdagen: the `parliament` module, `ParliamentApplication`, its own migrations and properties files
- **macro-frontend/** — Vite + React 18 + TypeScript + shadcn-ui dashboard (formerly the separate `macro-view-hub` repo, merged in via `git subtree`)
- **macro-backend/macro-infra/** — Docker Compose files (`docker/`) and microk8s manifests (`k8s/`)

## What has moved, and what is still in the monolith

The backend is being split into one service per domain ([ADRs](docs/adrs/README.md), [migration guide](docs/microservice-migration-guide.md)).

| | `regional-statistics-service` | `macro-series-service` | `parliament-service` | `macro-analyzer` (the monolith) |
| --- | --- | --- | --- | --- |
| Domains | `regionalstatistics` | `macroseries` | `parliament` | `election` |
| Host port | 8082 | 8083 | 8084 | 8080 |
| Database and role | `regional_statistics`, role `regional_statistics` | `macro_series`, role `macro_series` | `parliament`, role `parliament` | `macroanalyzer`, role `macrouser` (superuser) |
| Manual scrape paths | `/macro-analyzer/scrape/regional-statistics/{enqueue,enqueue-all,scrape-from-queue}` | `/macro-analyzer/scrape/macro-series/{enqueue,enqueue-all,scrape-from-queue}` | `/macro-analyzer/scrape/parliament/{enqueue,enqueue-all,scrape-from-queue}` | `/macro-analyzer/{enqueue,enqueue-all,scrape-from-queue}` |
| Migrations | `db/platform` (from `macro-platform`, history table `flyway_platform_schema_history`), then its own `db/migration` | The same | The same | `db/migration`, V1 to V37 |

Everything below about a "module" holds in all the applications; the rest of this file describes the monolith unless it names a service.

**Databases: port 31274, on `localhost` and on `192.168.154.7`, is the microk8s cluster's Postgres NodePort and its real database.** No default points there: both applications' properties (the monolith's `dev` profile included) and the jOOQ codegen use the Compose Postgres on `localhost:5432`, and each application's `BaseConfigurationTest` pins that. To use the cluster's database, pass the URL deliberately (`SPRING_DATASOURCE_URL`). Only `k8s/java/deployment.yaml` names it, because the pod has no local Postgres.

## Build & Test Commands

### Toolchain requirements

- **JDK 25** for the backend (installed via SDKMAN). If `java -version` isn't 25 (e.g. an IDE exported an older `JAVA_HOME`), set `JAVA_HOME=~/.sdkman/candidates/java/current` for Maven commands.
- Lombok is registered under `annotationProcessorPaths` in the compiler plugin because JDK 23+ no longer runs annotation processors found on the classpath; without it the build fails with `cannot find symbol` errors (`log`, getters/builders).
- **Node 20.19+ or 22.12+** for the frontend (Vite 7). Node 18 fails with `crypto.hash is not a function`.
- `mvnw` and the `*.sh` scripts are committed as executable. The root `.gitattributes` keeps them (and `.githooks/*`) LF even under `core.autocrlf=true`, so `./mvnw ...` works; if an old checkout still has CRLF copies, delete them and `git checkout -- .` to refresh.
- Docker must be running for Postgres and for `mvn test` (TestContainers).

### Starting the local stack (in this order)

**Default: Docker Compose** (from repo root; see "Docker" below for what it runs):

1. **Check what is already running:** `docker ps` and `lsof -nP -iTCP -sTCP:LISTEN | grep -E ':(8080|8081|8082|8083|8084|5173)'`. On a fresh clone, also `cp macro-backend/macro-series-service/.env.example macro-backend/macro-series-service/.env`: step 3 fails with `couldn't find env file` without it. The keys in it can stay empty until you scrape Riksbanken or Fed series. An existing clone has to move its file instead, or step 3 fails the same way: `mv macro-backend/macro-analyzer/.env macro-backend/macro-series-service/.env`.
2. **Build the backend images:** `(cd macro-backend && ./mvnw clean package && docker build -t macro-analyzer macro-analyzer && docker build -t regional-statistics-service regional-statistics-service && docker build -t macro-series-service macro-series-service && docker build -t parliament-service parliament-service)`.
3. **Start everything:** `docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --build`. Open `http://localhost:8081`. Flyway migrations run on startup. On an **empty** volume, Postgres creates the `regional_statistics`, `macro_series` and `parliament` databases and roles from `macro-infra/docker/postgres-init/`. An **existing** volume doesn't run that, and a service fails to start without its database. So first start Postgres if it isn't running (the same compose command with `up -d postgres`), then run `docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh` (it can be re-run; add `-e REGIONAL_STATISTICS_DB_PASSWORD=...`, `-e MACRO_SERIES_DB_PASSWORD=...` or `-e PARLIAMENT_DB_PASSWORD=...` after `-i` if you changed a password). A new database has no data, so `/omraden` and the charts stay empty until a scrape: see "Data" in the [migration guide](docs/microservice-migration-guide.md#data). The monolith's V34 and V36 dropped its copies of those tables, so there is nothing left to restore from. **`parliament` is different on an existing volume: restore it, don't re-scrape.** The guide's "Restoring parliament" has the commands; do that before the old tables are dropped.

**For working on the code**, run the backend with `mvnw` and the frontend with Vite. Vite proxies `/macro-analyzer` to the monolith on 8080 and the moved routes to `regional-statistics-service` on 8082, `macro-series-service` on 8083 and `parliament-service` on 8084, so the start order doesn't matter:

1. **Postgres** (from repo root): `docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up -d`. It listens on `localhost:5432`. **It can't run next to the full stack's Postgres as a second one:** both Compose files define the same `postgres` service (container `macrohub-postgres`, port 5432, Compose project `docker`, volume `docker_pgdata`). With the full stack up, this command leaves its Postgres running and warns about orphan containers (ignore it, and don't pass `--remove-orphans`), and you share that database, so only stop the stack's containers that hold 8080, 8082, 8083 or 8084. If `.env` sets `REGIONAL_STATISTICS_DB_PASSWORD`, `MACRO_SERIES_DB_PASSWORD` or `PARLIAMENT_DB_PASSWORD`, pass the same `--env-file`, or Compose re-creates the container (on the same volume). Anything else on port 5432 makes it fail to start, and a `k port-forward` to 5432 would give the applications the cluster's database.
2. **Backend** (from `macro-backend/`): `SPRING_DATASOURCE_PASSWORD=macropassword JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl macro-analyzer -am spring-boot:run -Dspring-boot.run.profiles=dev`. `-am` builds `macro-platform` with it; only the application module actually runs (`spring-boot.run.skip` in the poms). It serves on 8080 once `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/macro-analyzer/municipalities/elections` returns 200 or 204. Election reads party colours from `parliament-service` on `localhost:8084` and uses Valmyndigheten's until that answers.
3. **Frontend** (from `macro-frontend/`): `PATH=~/.nvm/versions/node/v22.22.1/bin:$PATH npm run dev`. Open `http://localhost:5173`.

**To work on `regional-statistics-service`** (from `macro-backend/`, with the Compose Postgres on 5432; stop the full stack's service container first if it holds 8082): `JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl regional-statistics-service -am spring-boot:run`. It needs no profile and no `.env`. Its base properties use `localhost:5432/regional_statistics`. For any other database, set `SPRING_DATASOURCE_URL` explicitly.

**To work on `macro-series-service`** (from `macro-backend/`, with the Compose Postgres on 5432; stop the full stack's service container first if it holds 8083): `JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl macro-series-service -am spring-boot:run`. It needs no profile. The working directory is `macro-series-service/`, so `.env` there is picked up, which only a Riksbanken or Fed scrape needs. Its base properties use `localhost:5432/macro_series`. For any other database, set `SPRING_DATASOURCE_URL` explicitly.

**To work on `parliament-service`** (from `macro-backend/`, with the Compose Postgres on 5432; stop the full stack's service container first if it holds 8084): `JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl parliament-service -am spring-boot:run`. It needs no profile and no `.env`. Its base properties use `localhost:5432/parliament`. For any other database, set `SPRING_DATASOURCE_URL` explicitly.

### Backend (from `macro-backend/`)

```bash
# Build (each application's jar is <module>/target/<module>-1.0.0.jar, with macro-platform inside)
mvn clean package

# Run all tests of all modules
mvn test

# Run a single test class
mvn test -Dtest=MacroSeriesControllerTest

# Run a single test method
mvn test -Dtest=MacroSeriesControllerTest#shouldReturnOk_whenValidBundleRetrieved

# Generate the JOOQ models of macro-platform and macro-analyzer from the database schema. The phase is compile, not generate-sources: macro-analyzer can
# only resolve macro-platform once that has been compiled. The database must be at the latest migration: the command
# deletes the classes of tables it doesn't find. The default URL is the Compose Postgres (either Compose file,
# localhost:5432/macroanalyzer). For any other database, add -Djooq.codegen.url=...
mvn -P jooq-codegen compile

# Generate regional-statistics-service's JOOQ model. Its own profile, so -am leaves macro-platform's model alone. The
# default is the Compose Postgres' localhost:5432/regional_statistics, with that service's role
mvn -P jooq-codegen-regional-statistics compile -pl regional-statistics-service -am

# The same for macro-series-service, whose default is localhost:5432/macro_series
mvn -P jooq-codegen-macro-series compile -pl macro-series-service -am

# The same for parliament-service, whose default is localhost:5432/parliament
mvn -P jooq-codegen-parliament compile -pl parliament-service -am

# Run locally against the Docker Postgres (localhost:5432)
SPRING_DATASOURCE_PASSWORD=macropassword ./mvnw -pl macro-analyzer -am spring-boot:run -Dspring-boot.run.profiles=dev
```

A fresh database has series configs but no data points (`chart-data` returns 204). Populate everything with `macro-backend/macro-infra/scripts/scrape-all.sh [gateway]` (default gateway `http://localhost:8081`), which enqueues everything and drains each queue, per service and in parallel. There is no global `enqueue-all` any more: the monolith's only covers what it still holds. For one service, `POST .../enqueue-all` followed by `POST .../scrape-from-queue` on its paths (see the table at the top).

Flyway migrations run automatically on application startup via `spring-boot-starter-flyway` (Boot 4 no longer auto-configures Flyway from `flyway-core` alone; there is no Flyway Maven plugin).

### Configuration profiles

These are the monolith's. `regional-statistics-service`, `macro-series-service` and `parliament-service` each have one `application.properties`, with defaults for the Compose Postgres and no profiles besides `test`.

- `application.properties` — base config; datasource points at the Compose Postgres (`localhost:5432/macroanalyzer`)
- `application-dev.properties` — sets the same datasource URL, pinned with the base one by `BaseConfigurationTest`
- `application-test.properties` — TestContainers datasource
- `prod` profile (used by the Dockerfile and `docker-compose-all.yml`) has no properties file; config comes from env vars, and without `SPRING_DATASOURCE_URL` the base `localhost:5432` applies, which inside a container is nothing
- Riksbanken and Fed scrapes require `riksbanken.prod.subscription.key` / `fed.prod.subscription.key` (env: `RIKSBANKEN_PROD_SUBSCRIPTION_KEY`, `FED_PROD_SUBSCRIPTION_KEY`). They are `macro-series-service`'s, and only it gets them
  - Locally, put them in `macro-backend/macro-series-service/.env` (copy `.env.example`; gitignored). Spring loads it via `spring.config.import=optional:file:.env[.properties]`, relative to the working directory, which is `macro-series-service/` under `spring-boot:run` (run a jar from there too). The monolith no longer reads a `.env`
  - For `docker-compose-all.yml`, pass `--env-file macro-backend/macro-series-service/.env`. That one file also holds Compose's optional database passwords, so every service's Compose command uses it
  - `.githooks/pre-commit` rejects commits that stage `.env` files (even via `git add -f`). Enable it once per clone: `git config core.hooksPath .githooks`. The hook is opt-in, so CI's `frontend` job also fails if any `.env` file is tracked (same rule: `.env`, `.env.<anything>`, except `.env.example`)

### Frontend (from `macro-frontend/`)

```bash
npm install
npm run dev        # Vite dev server
npm run build      # production build
npm run build:dev  # development-mode build
npm run lint
npm run typecheck  # tsc --noEmit for tsconfig.app.json (src/) and tsconfig.node.json (vite.config.ts, tailwind.config.ts); vite build does not type-check
npm run preview    # serve the production build
```

- Uses Recharts for charts, TanStack Query, React Router; `@/` aliases `src/`
- Kommun/region/valkrets maps (`ChoroplethMap`) render `public/geo/kommuner.topo.json`, `lan.topo.json` and `valkretsar.topo.json` with d3-geo. They are generated from SCB's CC0 boundary shapefiles by `scripts/build-geo.sh` (run from `macro-frontend/`; needs network and `npx`), and the outputs are committed. Valkretsar are dissolved from the kommun layer via `scripts/valkretsar.csv` (kommunkod → valkrets); update it if the valkrets division changes
- DeSO/RegSO boundaries for the Områdesstatistik map are in `public/geo/deso_2018.topo.json`, `deso_2025.topo.json`, `regso_2020.topo.json` and `regso_2025.topo.json` (object name = file stem; properties `code` (desokod/regsokod without version suffix), `name` (`regsonamn`, or `"<kommunnamn> <desokod>"` for DeSO), `kommunkod`, and on DeSO also `regsokod`). They are generated from SCB's WFS (`geodata.scb.se/geoserver/stat/wfs`, CC0) by `scripts/build-geo-scb-areas.sh` (run from `macro-frontend/`; needs network, `npx` and `node`, ~2 min). The script fails if a layer's feature count differs from the WFS hit count or a code is missing/duplicated. SCB's 2025 layers extend into territorial waters, so the script clips them to the dissolved land outline of DeSO 2018 / RegSO 2020 (lakes stay inside areas in every version). Simplification is Douglas-Peucker with a tolerance scaled by area (~4 m for inner-city areas, up to 150 m for rural ones) to keep the files at ~3.0 MB (DeSO) and ~1.9 MB (RegSO); `quantization=1e6` keeps vertices within ~1.5 m for street-level zoom
- The Områdesstatistik page (`/omraden`, `src/pages/RegionalStatistics.tsx`, components in `src/components/regionalmap/`) is lazy-loaded so MapLibre GL (`maplibre-gl`, pinned exact; its worker is set via `setWorkerUrl` from a `?url` import because bundling breaks the library's own lookup) stays out of the main bundle. The basemap is OpenFreeMap's `positron` style (no API key; attribution OpenFreeMap/OpenMapTiles/OpenStreetMap comes from the style, SCB is added). The values' `geoVersion` picks which boundary file is fetched (TanStack Query, cached per version). Areas are coloured via feature-state: single-sign values in up to 9 blue classes (breaks at 1/7..5/7 and p90, p97, p99, or the mirror p1, p3, p10 and 2/7..6/7 when `median - p1 >= 1.5 * (p99 - median)` and `p1 > min`, so a long tail is not one wide class; ties can give fewer), values with both signs (e.g. flyttnetto) in 7 red–gray–blue diverging classes (3 quantile classes per side, gray around zero). Borders follow the class too: `borderColor` in `classification.ts` picks white or dark, whichever contrasts more with the fill as composited over the basemap, and `line-opacity` falls off below zoom 8 so country zoom doesn't turn grey. URL state: `l` (DeSO|RegSO), `i` (indicator id), `y` (year), `a` (area code); an indicator without DeSO data is shown at RegSO. Address search uses Photon (`photon.komoot.io`, bbox around Sweden, results filtered to `countrycode=SE`; Photon rejects `lang=sv`, so no lang is sent) and finds the containing area with d3-geo `geoContains`
- shadcn-ui components live in `src/components/ui/` (configured via `components.json`)
- Backend base URL is `rootUrl` in `src/models/Constants.ts`, set at build time by `VITE_API_URL` (trailing slash stripped). Unset or empty means same origin (relative `/macro-analyzer/...` URLs), which nginx and the Vite proxy serve. It calls `/macro-analyzer/series-config`, `/macro-analyzer/chart-data` and the parliament, election and `regional-statistics` endpoints
- The dev server listens on port 5173. `vite.config.ts` proxies `/macro-analyzer` to the backends on `localhost` (8082, 8083 and 8084 for the moved routes, 8080 for the rest), split per domain the same way as the nginx routes; `vite preview` (port 4173) uses the same proxy. A `.env.development` can't set these: the pre-commit hook and CI reject it. Backend CORS still allows all origins, for a build with `VITE_API_URL` set. That URL must be a gateway (nginx or Vite), not a backend: the monolith on `:8080` answers 404 on `/macro-analyzer/regional-statistics/*`, `/macro-analyzer/parliament/*`, `/macro-analyzer/series-config` and `/macro-analyzer/chart-data`
- Package manager is npm; `package-lock.json` is the only lockfile (don't add `bun.lockb`/`yarn.lock`)

### Docker (from repo root)

```bash
# Dependencies only (PostgreSQL on localhost:5432, the same service as the full stack's, so not at the same time as a second one)
docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up

# Full stack (PostgreSQL + monolith on :8080 + regional-statistics-service on :8082 + macro-series-service on :8083 + parliament-service on :8084 + frontend on :8081) — build the backend images first:
#   (cd macro-backend && ./mvnw clean package && docker build -t macro-analyzer macro-analyzer && docker build -t regional-statistics-service regional-statistics-service && docker build -t macro-series-service macro-series-service && docker build -t parliament-service parliament-service)
# The frontend image is built by compose (add --build after frontend changes)
docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up
```

- The frontend container (`macro-frontend/Dockerfile`) builds with Node 22 and serves `dist/` with nginx on 8081; `nginx/default.conf.template` falls back to `index.html` for `BrowserRouter` routes
- The image is built with `VITE_API_URL` empty (build arg, override with `--build-arg VITE_API_URL=...`, which must be a gateway, because no single backend serves every route), so the browser calls the backend on the frontend's own origin and nginx proxies it per domain, each route with its own variable (`scheme://host:port`, no trailing slash; the defaults are the compose services, `http://regional-statistics:8082`, `http://macro-series:8083` and `http://parliament:8084` for the moved routes and `http://macro-analyzer:8080` for the rest):
  - `MACRO_SERIES_URL`: `/macro-analyzer/series-config`, `/macro-analyzer/chart-data`, `/macro-analyzer/scrape/macro-series/`
  - `REGIONAL_STATISTICS_URL`: `/macro-analyzer/regional-statistics/`, `/macro-analyzer/scrape/regional-statistics/`
  - `PARLIAMENT_URL`: `/macro-analyzer/parliament/`, `/macro-analyzer/scrape/parliament/`
  - `ELECTION_URL`: `/macro-analyzer/municipalities/`, `/macro-analyzer/regions/`, `/macro-analyzer/constituencies/`
  - `BACKEND_URL`: the catch-all `/macro-analyzer/`, which carries the monolith's manual enqueue and scrape endpoints
- A new variable needs a default in the Dockerfile, because the entrypoint leaves an unset one as the literal `${X}`. A variable set to an empty value makes nginx exit at startup (`invalid number of arguments in "set" directive`), so write literal values in Compose rather than `${X:-}`. The proxy settings, including the 30-minute `proxy_read_timeout`, are set once on the `server` and inherited by every route
- The nginx image's envsubst entrypoint renders the template; each upstream is resolved per request with the resolver from `/etc/resolv.conf` (`NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1`), so nginx starts before the backend is up. nginx's resolver ignores resolv.conf search domains, so outside compose (e.g. a future k8s deployment; `k8s/` has no frontend manifest today) give the variables a fully qualified name such as `http://macro-analyzer-service.default.svc.cluster.local:8080`

## Architecture

The backend is split into **modules by domain**. Inside each module it follows **Hexagonal Architecture (Ports & Adapters)** with **Domain-Driven Design**. Every class lives in a module under `com.kg.macroanalyzer`, except the application classes in the root package and the generated jOOQ models. `election` runs in the monolith; `regionalstatistics`, `macroseries` and `parliament` each run on their own. The Maven modules cut across this: `scrape`, `shared` and their jOOQ model are built in `macro-platform`, `regionalstatistics` in `regional-statistics-service`, `macroseries` in `macro-series-service`, `parliament` in `parliament-service` and `election` in `macro-analyzer`. The packages are the same as before the split, so component scanning from `MacroAnalyzerApplication` finds `macro-platform`'s beans.

```
  REST ──→ adaptors.http ──→ port.in ──→ service ──→ port.out ←── adaptors.postgres ──→ PostgreSQL
                                           │
                                           └──→ domain

  InsertionAdaptor, CronEnqueueAdaptor, CronScrapeAdaptor ──→ InPort ──→ EnqueueService ──→ ScrapeQueuePort
                                                                    └──→ ScrapeService
  ScrapeService ──→ RegisteredScrapeFlow (picked by the config's scrape_flow id, built in each module's adaptors.config)
      Preparer ──→ ExistingFinder ──→ Scraper ──→ WebPort ←── adaptors.web ──→ external API
                                      Persister ──→ port.out, then ScrapeQueuePort.markAsDone

  election ──→ PartyColorPort ←── adaptors.web ──→ GET /macro-analyzer/parliament/parties (cached, refreshed in the background)
```

### Package structure (`com.kg.macroanalyzer`)

The backend is organised by domain. Every module (`scrape`, `macroseries`, `regionalstatistics`, `parliament`, `election`) uses `domain/`, `application/port/in/`, `application/port/out/`, `application/service/` and `adaptors/{http,postgres,web,config,cron}/`; besides them there is only `shared/`, the generated jOOQ models and the application class in the root package.

- **`scrape/`** (Maven module `macro-platform`) — the scrape core, independent of the data sources:
  - `domain/` — `ScrapeQueueItem`, `ScrapeResult`, `SeriesConfig` (every scrape flow's config, including the `scrapeFlow` id that routes it)
  - `application/port/in/` — `InPort` (enqueue, enqueue-all, scrape-from-queue) and its `ScrapeRun` result; `application/port/out/` — `ScrapeQueuePort` (queue reads/writes, `markAsDone`) and `SeriesConfigPort`
  - `application/service/` — `InPortImpl`, `ScrapeService`, `EnqueueService`, and in `scrapeflow/` the `ScrapeFlow` pipeline (`Preparer` → `ExistingFinder` → `Scraper` → `Persister`) and `RegisteredScrapeFlow` (see below)
  - `adaptors/http/` — `InsertionAdaptor` (enqueue/scrape endpoints); `adaptors/cron/` — `CronScrapeAdaptor` drains the queue every 60s, `CronEnqueueAdaptor` enqueues all series every minute during the 22:00 hour; `adaptors/postgres/` — queue and series-config repositories; `adaptors/config/` — preparer, series-config list and scheduler beans
- **`macroseries/`** (Maven module `macro-series-service`, with its own jOOQ model in `macroseries.jooq.generated`) — chart time series (Riksbanken, Fed and Eurostat rates, bonds, bills, exchange rates and key figures):
  - `domain/` — `MacroSeries`, `MacroPoint`, `AlignedBundle`, `InvalidBundleDimensionException`, `MacroSeriesScrapeFlows` (the `MACRO_POINT` flow id)
  - `application/port/in/` — `MacroSeriesQueries` (aligned chart data, chart series configs) and its DTOs (`BuildChartDataParams` with the `Strategy` and `TimeFrame` enums, `ChartSeriesParam`, `ChartData`, `ChartDataWithLabels`); `application/port/out/` — `MacroSeriesRepositoryPort` and the scrape payload `ConfigWithMacroPoints`
  - `application/service/` — `MacroSeriesService` (the chart list is the configs of the `MACRO_POINT` flow), `LabelGenerationService`, in `bundleformat/` `TimeFilterService`, `AlignmentService` and the sample and time-filter strategies, and in `scrape/` `ExistingMacroPointFinder`, `MacroPointScraper` and `MacroPointPersister`
  - `adaptors/http/` — `MacroSeriesController` (`/series-config`, `/chart-data`), `SeriesConfigResponse` (a `/series-config` entry, without `scrapeFlow`) and `ColorSelectionStrategy`; `adaptors/postgres/` — `MacroSeriesPostgresAdaptor`, the per-table rate, bond, bill, exchange-rate, Fed and Eurostat repositories, and `MacroPointUpsert`, which all their writers use; `adaptors/web/` — `MacroPointWebAdaptor` and `MacroPointAdaptorFlowFactory`, which routes to the Riksbanken, Fed or Eurostat connection builder and response parser; `adaptors/config/` — the web adaptor bean (with the Riksbanken/Fed subscription keys) and the scrape flow
- **`regionalstatistics/`** (Maven module `regional-statistics-service`, with its own jOOQ model in `regionalstatistics.jooq.generated`) — SCB DeSO/RegSO statistics:
  - `domain/` — `GeoVersion`, `ScbLevel`, `ScbIndicator`, `ScbValues` and the other SCB value types, `RegionalStatisticsScrapeFlows` (the `SCB_REGIONAL` flow id)
  - `application/port/in/` — `RegionalStatisticsQueries` (indicators and values) and its DTOs; `application/port/out/` — `RegionalStatisticsRepositoryPort`, the scrape payload `ConfigWithScbIndicators` and the SCB web requests
  - `application/service/` — `RegionalStatisticsService`, and in `scrape/` the scrape flow (`ExistingScbTableFinder`, `ScbRegionalScraper`, `ScbRegionalPersister`)
  - `adaptors/http/` — `RegionalStatisticsController` (`/regional-statistics/*`); `adaptors/postgres/` — `RegionalStatisticsPostgresAdaptor` and `ScbRegionalRepository`; `adaptors/web/` — SCB web adaptors, rate limiter, retry, connection builder and JSON-stat parsers; `adaptors/config/` — the SCB web beans and the scrape flow
- **`parliament/`** (Maven module `parliament-service`, with its own jOOQ model in `parliament.jooq.generated`) — Riksdagen members, activity, parties and elections:
  - `domain/` — `MemberOfParliament`, `MemberActivity`, `Party`, `Election` and the other Riksdagen value types, `ParliamentScrapeFlows` (the `MEMBER_OF_PARLIAMENT` and `MEMBER_ACTIVITY` flow ids)
  - `application/port/in/` — `ParliamentQueries` (parties, elections, composition, members) and its DTOs; `application/port/out/` — `ParliamentRepositoryPort`, the scrape payloads `ConfigWithMemberOfParliament`/`ConfigWithMemberActivity` and `MemberActivityRequest`
  - `application/service/` — `ParliamentService`, and in `scrape/` the member and member-activity finders, scrapers and persisters
  - `adaptors/http/` — `ParliamentController` (`/parliament/*`); `adaptors/postgres/` — `ParliamentPostgresAdaptor`, `SweParMemberRepository`, `SweParActivityRepository` and `RiksdagElectionRepository`; `adaptors/web/` — `MembersOfParliamentWebAdaptor`, `RiksdagenActivityWebAdaptor`, the Riksdagen connection builders and response parsers; `adaptors/config/` — the Riksdagen web beans and both scrape flows
- **`election/`** — Valmyndigheten kommun (KF), region (RF) and Riksdag valkrets (RD) elections:
  - `domain/` — `LocalElectionType`, `LocalElectionResult`, `LocalPartyResult`, `ElectedMember`, `Governance`, `ElectionScrapeFlows` (the `LOCAL_ELECTION` flow id)
  - `application/port/in/` — `ElectionQueries` (results per type and year, one area's detail) and its DTOs; `application/port/out/` — `ElectionRepositoryPort`, `PartyColorPort`, the scrape payload `ConfigWithLocalElections`, `LocalElectionIndexRequest` and `LocalElectionFile`
  - `application/service/` — `LocalElectionService` (party colours come from `PartyColorPort`), and in `scrape/` `ExistingLocalElectionFinder`, `LocalElectionScraper` and `LocalElectionPersister`
  - `adaptors/http/` — `ElectionController` (`/municipalities/*`, `/regions/*`, `/constituencies/*`); `adaptors/postgres/` — `ElectionPostgresAdaptor` and `LocalElectionRepository`; `adaptors/web/` — `RefreshingPartyColorAdaptor` and `ParliamentPartiesClient` (party colours over REST), the Valmyndigheten index and result web adaptors, retry, connection builder and parsers; `adaptors/config/` — the Valmyndigheten web beans and the scrape flow
- **`shared/`** (Maven module `macro-platform`) — used by every module, depends on none of them: `config/` (CORS, Jackson, JDBC, Flyway catalog version) and `web/` (`WebPort`, `ScrapeException`, `WebAdaptorFlow`, `ConnectionBuilder`/`Getter`/`ResponseParser`, `HttpGetter`, `ZipEntryGetter`, `HttpResponses`)
  - Jackson 3 (`tools.jackson`) is used throughout. `JacksonConfiguration` (in `shared/config/`) defines the `JsonMapper` bean, which Boot's MVC auto-config uses for the message converter; don't add `@EnableWebMvc`, which would turn that auto-config off. The bean keeps `WRITE_DATES_AS_TIMESTAMPS` enabled because the frontend expects date labels as arrays (`[1994,6]`).
  - The backend is JSON-only, so `spring.web.resources.add-mappings=false` (base `application.properties`, every profile) turns off Boot's `/**` and `/webjars/**` resource handlers: nothing on a static location is served, and unknown paths answer 404 for every method, also CORS requests (the handler would answer them with 200 or 403). `NoStaticResourceHandlersHttpTest` pins this
- **`jooq/generated/`** (`macro-analyzer`) and **`platform/jooq/generated/`** (`macro-platform`) — Auto-generated JOOQ classes (do not edit manually). `macro-platform`'s model holds `series_configuration`, `scrape_action_queue` and `flyway_schema_history`, for `scrape` and `shared`; the other holds every other table of the monolith's database, for `election`, including parliament's tables until they are dropped (nothing reads those). The tables are selected in each module's `jooq-codegen` profile

### Layers inside a module

- `domain/` — records and enums of the domain (Lombok only, no Spring).
- `application/port/in/` — what the module offers: query interfaces (`*Queries`, `InPort`) and their DTOs/parameters.
- `application/port/out/` — what the module needs: repository ports (`*RepositoryPort`) and the request/config-with-data records its scrapers pass around.
- `application/service/` — implementations of the in-ports; `application/service/scrape/` holds the module's `ExistingFinder`/`Scraper`/`Persister`.
- `adaptors/http/` (controllers), `adaptors/postgres/` (`*PostgresAdaptor` implementing the repository port, plus jOOQ repositories), `adaptors/web/` (`WebPort` implementations, connection builders, response parsers), `adaptors/config/` (`*WebAdaptorConfiguration` for the `WebPort` beans, `*ScrapeFlowConfiguration` for the scrape flow), `adaptors/cron/` (scrape only).

### How a scrape flow plugs in

Each domain's `*ScrapeFlowConfiguration` builds a `ScrapeFlow` from the shared `Preparer` and its own finder, scraper and persister, and registers it as a `RegisteredScrapeFlow` bean under a flow id. The ids are plain strings that each module owns as constants in its `domain/` (`MacroSeriesScrapeFlows.MACRO_POINT`, `ParliamentScrapeFlows.MEMBER_OF_PARLIAMENT`/`MEMBER_ACTIVITY`, `ElectionScrapeFlows.LOCAL_ELECTION`, `RegionalStatisticsScrapeFlows.SCB_REGIONAL`); `scrape` never names them. Every `series_configuration` row says which flow scrapes it in `scrape_flow` (NOT NULL, V31). `ScrapeService` routes a queue item by name → `SeriesConfig` (through the `SeriesConfigPreparer`) → `scrapeFlow` → flow; an item whose name matches no config is `FAILED` with a warning and stays pending. At startup it fails if two flows share an id or a config's `scrapeFlow` has no registered flow (the message names the config and the id), and only warns for a registered flow that no config uses. The configs are read once at startup, so a new row needs a restart. Persisters write through their module's repository port and then call `ScrapeQueuePort.markAsDone` (with nothing to write they only mark it done). The two don't share a transaction. An item that throws is `FAILED`, doesn't stop the others and stays pending, so the cron retries it about once a minute, re-fetching from the source.

### Dependency rules (`ArchitectureTest`)

Each application has its own `src/test/java/com/kg/macroanalyzer/ArchitectureTest.java` (ArchUnit, part of `mvn test`). `regional-statistics-service`'s, `macro-series-service`'s and `parliament-service`'s have the same rules for their one module, and also only let `adaptors.postgres` use its jOOQ model. `macro-analyzer`'s enforces:

- every class except `MacroAnalyzerApplication` resides in a module listed in `MODULES`/`SHARED_MODULES` or in `GENERATED_PACKAGES` (`jooq.generated`, `platform.jooq.generated`);
- `shared` depends on no module;
- a module's `domain` and `application` do not depend on its `adaptors`, and no `application.port` depends on any module's `application.service` (`scrape`'s included);
- a domain module (`DOMAIN_MODULES`) only depends on itself, `scrape`, `shared` and `jooq.generated`, plus what `DOMAIN_MODULE_EXTRA_DEPENDENCIES` allows, which today is nothing: domain modules don't use each other's code, and election reads the parliament service's party list over HTTP (see "Kommun, region and Riksdag elections"). No module may use another module's out-ports, services or adaptors.

### Adding a new domain

1. Create `com.kg.macroanalyzer.<module>` with `domain/`, `application/port/in/`, `application/port/out/`, `application/service/` (and `service/scrape/`) and `adaptors/`.
2. Define the in-port (`*Queries`) and out-port (`*RepositoryPort`); implement them in a service and a `*PostgresAdaptor`.
3. Add a controller in `adaptors/http/` that only uses the in-port.
4. Add a `*WebAdaptorConfiguration` for the `WebPort` beans and a `*ScrapeFlowConfiguration` that registers a `RegisteredScrapeFlow` under a new flow id.
5. Define that flow id as a constant in the module's `domain/` (e.g. `<Module>ScrapeFlows`). Only `MACRO_POINT` configs appear in `/series-config`, so a new flow stays out of the chart picker without further changes.
6. Add the module to `MODULES` and `DOMAIN_MODULES` in `ArchitectureTest` (and to `DOMAIN_MODULE_EXTRA_DEPENDENCIES` only if it must use another module's in-port).
7. Add a Flyway migration with the module's tables and its `series_configuration` row, then regenerate the jOOQ models. The row needs a `scrape_url`, a `category` and its `scrape_flow` (the id from step 5); startup fails if no flow is registered under that id.

### REST API Endpoints

- `GET /macro-analyzer/series-config` — on `macro-series-service`: list the chart series configurations, in `id` (seed) order
- `POST /macro-analyzer/chart-data` — on `macro-series-service`: retrieve aligned chart data (400 when `strategy`, `timeFrame` or `chartSeriesParams` is missing)
- `POST /macro-analyzer/enqueue?name={series}` — enqueue one of the monolith's series for scraping (202; a name it doesn't know, such as a moved series, also answers 202 and does nothing)
- `POST /macro-analyzer/enqueue-all` — enqueue all of the monolith's series (202)
- `POST /macro-analyzer/scrape-from-queue` — execute the monolith's pending scrapes (200, 409 while one is running)
- `POST /macro-analyzer/scrape/regional-statistics/enqueue?name=`, `.../enqueue-all`, `.../scrape-from-queue` — the same on `regional-statistics-service`
- `POST /macro-analyzer/scrape/macro-series/enqueue?name=`, `.../enqueue-all`, `.../scrape-from-queue` — the same on `macro-series-service`
- `POST /macro-analyzer/scrape/parliament/enqueue?name=`, `.../enqueue-all`, `.../scrape-from-queue` — the same on `parliament-service`
- `GET /macro-analyzer/parliament/elections` — on `parliament-service`, as are the other `/parliament/*` endpoints: official seat results per election (newest first)
- `GET /macro-analyzer/parliament/composition` — live seats per party from currently serving MPs
- `GET /macro-analyzer/parliament/parties` — party names, colours, ideology
- `GET /macro-analyzer/parliament/members?year={electionYear}` — members who sat in that term (default: latest)
- `GET /macro-analyzer/parliament/members/{intressentId}` — member detail (assignments, biography, contact, votes, speeches, motions)
- `GET /macro-analyzer/municipalities/elections?year=` / `GET /macro-analyzer/regions/elections?year=` / `GET /macro-analyzer/constituencies/elections?year=` — per kommun/region/Riksdag valkrets: seats, turnout, votes and seats per party (default: latest year)
- `GET /macro-analyzer/municipalities/{kommunkod}` / `GET /macro-analyzer/regions/{länskod}` / `GET /macro-analyzer/constituencies/{valkretskod}` — the same plus elected members and curated governance (none for valkretsar)
- `GET /macro-analyzer/regional-statistics/indicators` — SCB indicators with `availability` per level (`DeSO`, `RegSO`): years (newest first) and the boundary version (`geoVersion`) of each
- `GET /macro-analyzer/regional-statistics/values?indicator=&level=DeSO|RegSO&year=` — `{indicator, level, year, geoVersion, values: {areaCode: value}}` (default: latest year; 204 without data, 400 for an unknown level)

### Riksdagen (parliament) data

- All of this runs in `parliament-service`, on its own database and scrape lock. `MembersOfParliament` scrape upserts every person from `data.riksdagen.se/personlista` (JSONB, keyed on `intressent_id`); `MembersOfParliamentActivity` then fetches voting totals, speeches and motions per member of the latest two terms (~600 members, ~7 min, throttled)
- Election seat counts are curated in `riksdag_election_result` (Flyway seed from Valmyndigheten) because Riksdagen only stores each person's *current* party. Add a new election as a migration in `parliament-service` (rows in `riksdag_election` + `riksdag_election_result`). `/parliament/parties` has no `ORDER BY`, so it answers in the table's order, which the baseline keeps as the monolith left it (SD last)
- A re-scrape doesn't replace a local database: activity is only fetched for the latest two terms, and `riksdag_*` is curated. An existing volume restores from the monolith's tables, while they exist: "Restoring parliament" in the [migration guide](docs/microservice-migration-guide.md#restoring-parliament)
- Riksdagen configs (category `Riksdagen`, flows `MEMBER_OF_PARLIAMENT` and `MEMBER_ACTIVITY`) are in `parliament-service`'s database, so they aren't in `/series-config`, which `macro-series-service` serves from its own, and don't appear in the chart picker

### Kommun, region and Riksdag elections (Valmyndigheten)

- `MunicipalElections` (KF) and `RegionElections` (RF) scrape the final results from `resultat.val.se`: `index.md5` lists one zip per kommun/region, and only the `mandatfordelning` JSON inside each is parsed (votes, seats, elected members). First run downloads ~310 zips (~1.5 min); later runs skip files whose md5 is unchanged, so corrected files are picked up automatically
- `ParliamentElections` (RD) uses the same pipeline, but its single zip (`s/rd/..._00_RD.zip`) holds all 29 valkretsar under `valkretsLista`, so the parser returns one area per valkrets (area code `01`..`29`, Gotland is `09`). Valkrets seats include the utjämningsmandat assigned to that valkrets (they sum to 349). The ~200 MB per-district `rostfordelning` file in the zip is streamed past, not parsed
- `scrape_url` (`.../resultatfiler/val2026`) selects the election; point it at the next election's folder with a migration. Gotland has no regionfullmäktige
- Who governs (`local_governance`) has no open data source and starts empty; add rows by migration (party abbreviations as in the results). The frontend only shows the "styre" section where a row exists
- Configs use category `Valmyndigheten` and flow `LOCAL_ELECTION`. They are in the monolith's database too, so they are not in `/series-config` either
- Riksdag parties get the colours of parliament's `GET /macro-analyzer/parliament/parties` ([ADR 0005](docs/adrs/0005-cached-rest-between-services-and-no-message-broker.md)); other parties keep Valmyndigheten's colour, then gray. `RefreshingPartyColorAdaptor` (behind `PartyColorPort`) holds the list in memory, so a request never calls parliament. `ParliamentPartiesClient` fetches it on the adaptor's own daemon thread, independent of `scheduler.enabled`: first when the application is ready, then every `election.parliament.refresh-interval` (1h), or every `retry-interval` (15s) until a first list has loaded. `timeout` (5s) covers the whole exchange. A failed refresh, a 204 or an empty list keeps the list held; with no list, Valmyndigheten's colours are used. `election.parliament.url` (env `ELECTION_PARLIAMENT_URL`) defaults to `parliament-service` under `mvnw`, `http://localhost:8084`; Compose sets `http://parliament:8084`. The monolith has no `depends_on` on parliament and starts without it. With no list yet, the colours load at the next retry after parliament is up, at most 15 seconds later
- `macro-backend/contracts/parliament-parties.json` is the contract for that endpoint: the fields election reads. `ParliamentPartiesClientTest` and `PartiesContractHttpTest` hold each side to it, using only the file and HTTP. Changing `code` or `color` needs care, because `/parliament/parties` is an API between services

### SCB regional statistics (DeSO / RegSO)

- All of this runs in `regional-statistics-service`, on its own database and scrape lock. `ScbRegionalStatistics` (category `SCB`, flow `SCB_REGIONAL`) scrapes curated indicators from SCB's PxWeb API v2 (`scrape_url` `https://statistikdatabasen.scb.se/api/v2`, CC0). Indicators live in `scb_indicator` (seeded by the service's `V1`): `numerator`/`denominator` are JSONB lists of `{table, contents, select}`; every table dimension except `Region` and `Tid` must be selected, codes within a dimension are summed, a later source replaces an earlier one per year and version, and with a denominator the value is `sum(num) / sum(den) * multiplier`
- One SCB table holds every area version; the region code tells them apart: `DeSO2018` (`0114A0010`), `DeSO2025` (`0114A0010_DeSO2025`), `RegSO2020` (`0114R001`), `RegSO2025` (`0114R001_RegSO2025`). Data is requested per version with a PxWeb wildcard (`?????????`, `*_DeSO2025`, …) and chunked by year to stay below 150 000 cells per call. Years a version has no data for are 0/null placeholders in SCB; a (version, year) is only kept when a value is non-null and non-zero. `area_code` is stored without the suffix so it matches the boundary files
- `scb_table_state` keeps SCB's `updated` stamp per table; an indicator is skipped when all its tables are unchanged and it already has rows. Stamps are written once every indicator of the run is stored; a table whose fetch failed, and every table of an indicator whose write failed, loses its stamp so the indicators using it are fetched again. To change an existing indicator's selection, also delete its `scb_area_value` and `scb_availability` rows in the migration so it is re-fetched (whether an indicator has data is read from `scb_availability`, which the persister keeps in step with the values)
- Calls are spaced by `scb.delay-ms` (default 350 ms, SCB allows 30 calls per 10 s) and retried up to 8 times: some SCB servers intermittently answer "Non-existent table" (404/400) for tables that exist. A first full scrape takes 9 to 12 minutes (2.4 M rows, ~370 MB; 8 min 49 s when last measured) because SCB spends several seconds on each uncached DeSO request; a run with unchanged tables takes about 15 to 30 seconds
- A full scrape keeps every fetched value in memory until the persister runs (~2.4 M values plus the per-source maps; the container peaked at 846 MiB in a measured first scrape, on a JVM free to use a quarter of 31 GiB, so that's an upper bound rather than what it needs). The JVM has no `-Xmx` and the container no memory limit today; if either is added, leave room for this or the first full scrape can run out of memory

### Database

PostgreSQL 16, one database per service on one instance. The monolith's Flyway migrations are in `macro-analyzer/src/main/resources/db/migration/` (V1–V37). `regional-statistics-service`, `macro-series-service` and `parliament-service` run Flyway twice: `macro-platform`'s `db/platform` (`series_configuration`, `scrape_action_queue`) when `macro.platform.flyway.enabled=true`, then their own `db/migration`, whose `V1` is a squashed baseline with their config rows (a baseline at version 0 lets it run on the non-empty schema). JOOQ generates type-safe table access. Key tables: `series_configuration`, `scrape_action_queue`, `local_election_area`/`local_election_party_result`/`local_governance`, and parliament's old tables until a later migration drops them; in `parliament`, `swedish_members_of_parliament` (JSONB), `swedish_member_activity`, `riksdag_party`/`riksdag_election`/`riksdag_election_result`/`riksdag_organ` and its own `series_configuration` and `scrape_action_queue`; in `regional_statistics`, `scb_indicator`/`scb_area_value`/`scb_availability`/`scb_table_state` and its own `series_configuration` and `scrape_action_queue`; in `macro_series`, the 41 rate, bond, bill, exchange-rate, Fed and Eurostat tables and its own two.

The 41 tables in `macro_series` hold one row per series and date: `UNIQUE (date)`, or `(series_name, date)` in `fed_series` and `eurostat_national_debt_series`, from `macro-series-service`'s V2 (the column is `usd_sek_date` in `exchange_usd_sek` and `policy_rate_date` in `policy_rate_sweden`). `MacroPointScraper` keeps the scraped points whose value and date aren't both stored, and `MacroPointUpsert` writes them with `ON CONFLICT ... DO UPDATE`: a revised value replaces the old one and sets `updated`, while `id`, `global_id` and `created` stay. A write of a value already stored changes nothing, so a repeated or retried scrape, or two at once, can't add rows or rewrite them. A new chart series table needs the same constraint and writer.

Chart series in `macro_series`' `series_configuration` carry picker metadata, returned by `/series-config`: `category` (`Räntor`, `Statsobligationer`, `Statsskuld`, `Valuta`, `Nyckeltal USA`), `subcategory` (e.g. `Styrränta`, `Euromarknadsränta`, `Statsskuldväxel`, `Nominell`), `unit` (short Swedish display form taken from the source: `%`, `% av BNP`, `mn EUR`, `mn USD`, `mdr USD`, `SEK/USD`, `punkter`, `index`), `country` (Swedish name, null when not country-specific) and `maturity_months` (rates/bonds/bills only). A new chart series must set them, and `scrape_flow = 'MACRO_POINT'`, in its migration; the chart series are exactly the `MACRO_POINT` configs. `ChartSeriesConfigIntegrationTest` checks that every chart series has a unit and subcategory, and lists the exact category set and row count. `MonolithSeriesConfigIntegrationTest` checks the flow of every config still in the monolith, and `ParliamentApplicationTests` of the Riksdagen ones. `/series-config` lists the configs in `id` order, and `/chart-data` gives a series its colour from its place in that order rather than the request's, so the baseline keeps the monolith's ids.

## Testing

- JUnit 5 + Mockito for unit tests
- TestContainers (PostgreSQL) for integration tests — Docker must be running for `mvn test`
- Each Maven module has its own tests. `macro-platform` has the unit tests of `scrape` and `shared`. `SeriesConfigRepositoryTest` and `NoStaticResourceHandlersHttpTest` are in those packages but stay in `macro-analyzer`, because they need the application and its migrations (each service has its own copy of the latter). `TestSeriesConfigFactory` and `TestJsonReader` exist in several modules: a shared test-jar would be on the classpath as a jar, and `ArchitectureTest`'s `DoNotIncludeTests` would then import its classes as production code
- `ArchitectureTest` (ArchUnit, one in each application) imports all of `com.kg.macroanalyzer` from the classpath, so it covers `macro-platform` too; `importIncludesMacroPlatform` fails if that ever stops. It checks the module boundaries: every class outside the root package lives in a module, `shared` or a generated jOOQ package (so no layer-first package can come back), `shared` doesn't depend on any module, and a module's `domain`/`application` don't depend on its `adaptors`. A domain module (`DOMAIN_MODULES`) depends only on itself, `scrape`, `shared` and `jooq.generated`, and nothing outside it depends on it, apart from what `DOMAIN_MODULE_EXTRA_DEPENDENCIES` allows per module (another module's `application/port/in` and `domain` only; no entries today). Add a new module to `MODULES` (and to `DOMAIN_MODULES` for a business domain)
- Test fixtures in `src/test/resources/json/`
- `@ActiveProfiles("test")` activates `application-test.properties`

## Workflow

Agreed changes are implemented with the `/ship` skill (`.claude/skills/ship/SKILL.md`): an implementer sub-agent opens a PR, a separate reviewer sub-agent (Fable, or Opus when Fable credits run out) reviews it, then it is merged once the reviewer says LGTM. `main` is protected: every change needs a PR, and the `frontend` and `backend` jobs in `.github/workflows/ci.yml` (frontend: tracked-`.env` check, `npm run typecheck`, build, lint; backend: `mvn test`) must pass. No approval is required.

Larger decisions are recorded as ADRs in `docs/adrs/`. The planned split into services is tracked in `docs/microservice-migration-guide.md`, which is updated in the same PR as each step; until it is finished, the rest of this file describes the current monolith.

PRs also get an automatic Claude review comment from `.github/workflows/claude-review.yml` (comment-only, not a required check; a PR that changes that file is skipped, since the action requires it to match `main`). It needs the Claude GitHub App installed on the repo and a `CLAUDE_CODE_OAUTH_TOKEN` repo secret generated with `claude setup-token` (Pro/Max subscription).

## Commit Convention

Follows conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `config:`, `chore:`, `docs:`
