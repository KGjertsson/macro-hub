# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

macro-hub visualizes macro-economic trends from multiple data sources (Riksbanken, Eurostat, Federal Reserve, Swedish Parliament, Valmyndigheten, SCB). It consists of:

- **macro-backend/** — Java 25 / Spring Boot 4.1.2 backend, a Maven reactor (parent `pom.xml` and `mvnw` here):
  - **macro-platform/** — library with the `scrape` core and `shared` (no application class; its only migrations are `db/platform`, which every service runs first)
  - **regional-statistics-service/** — SCB DeSO/RegSO statistics: the `regionalstatistics` module, `RegionalStatisticsApplication`, its own migrations and properties
  - **macro-series-service/** — the chart series: the `macroseries` module, `MacroSeriesApplication`, its own migrations and properties, and the Riksbanken and Fed keys
  - **parliament-service/** — Riksdagen: the `parliament` module, `ParliamentApplication`, its own migrations and properties
  - **election-service/** — Valmyndigheten's elections: the `election` module, `ElectionApplication`, its own migrations and properties
  - **acceptance-tests/** — black-box tests of the built images, only over HTTP through the gateway (no main code, and it depends on no other module)
- **macro-frontend/** — Vite + React 18 + TypeScript + shadcn-ui dashboard (formerly the separate `macro-view-hub` repo, merged in via `git subtree`)
- **macro-backend/macro-infra/** — Docker Compose files (`docker/`) and `scrape-all.sh`, `acceptance-test.sh` and the CI-only `compose-smoke.sh` (`scripts/`)

## Services

The backend is one service per domain ([ADRs](docs/adrs/README.md), and the [migration guide's last version](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md) for how it got here).

| | `regional-statistics-service` | `macro-series-service` | `parliament-service` | `election-service` |
| --- | --- | --- | --- | --- |
| Domain | `regionalstatistics` | `macroseries` | `parliament` | `election` |
| Port (container and host) | 8082 | 8083 | 8084 | 8085 |
| Database and role | `regional_statistics` | `macro_series` | `parliament` | `election` |
| Compose service | `regional-statistics` | `macro-series` | `parliament` | `election` |
| Manual scrape paths | `/macro-analyzer/scrape/regional-statistics/{enqueue,enqueue-all,scrape-from-queue}` | `/macro-analyzer/scrape/macro-series/{...}` | `/macro-analyzer/scrape/parliament/{...}` | `/macro-analyzer/scrape/election/{...}` |

Every service runs `db/platform` (from `macro-platform`, history table `flyway_platform_schema_history`), then its own `db/migration`. Everything below about a "module" holds in all of them. The Java base package `com.kg.macroanalyzer` and the URL prefix `/macro-analyzer` are the project's old name, and every service keeps them.

**Databases:** every service's properties and every jOOQ codegen profile default to the Compose Postgres on `localhost:5432`, and each service's `BaseConfigurationTest` pins that. To use another database, pass the URL deliberately (`SPRING_DATASOURCE_URL`).

## Build & Test Commands

### Toolchain requirements

- **JDK 25** for the backend (installed via SDKMAN). If `java -version` isn't 25 (e.g. an IDE exported an older `JAVA_HOME`), set `JAVA_HOME=~/.sdkman/candidates/java/current` for Maven commands.
- Lombok is registered under `annotationProcessorPaths` in the compiler plugin because JDK 23+ no longer runs annotation processors found on the classpath; without it the build fails with `cannot find symbol` errors (`log`, getters/builders).
- **Node 20.19+ or 22.12+** for the frontend (Vite 7). Node 18 fails with `crypto.hash is not a function`.
- `mvnw` and the `*.sh` scripts are committed as executable. The root `.gitattributes` keeps them (and `.githooks/*`) LF even under `core.autocrlf=true`, so `./mvnw ...` works; if an old checkout still has CRLF copies, delete them and `git checkout -- .` to refresh.
- Docker must be running for Postgres and for `mvn test` (TestContainers).

### Starting the local stack (in this order)

**Default: Docker Compose** (from repo root; see "Docker" below for what it runs):

1. **Check what is already running:** `docker ps` and `lsof -nP -iTCP -sTCP:LISTEN | grep -E ':(8081|8082|8083|8084|8085|5173)'`. On a fresh clone, also `cp macro-backend/macro-series-service/.env.example macro-backend/macro-series-service/.env`: step 3 fails with `couldn't find env file` without it. The keys in it can stay empty until you scrape Riksbanken or Fed series.
2. **Build the backend images:** `(cd macro-backend && ./mvnw clean package && docker build -t regional-statistics-service regional-statistics-service && docker build -t macro-series-service macro-series-service && docker build -t parliament-service parliament-service && docker build -t election-service election-service)`.
3. **Start everything:** `docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --build`. Open `http://localhost:8081`. Flyway migrations run on startup. On an **empty** volume, Postgres creates the `regional_statistics`, `macro_series`, `parliament` and `election` databases and roles from `macro-infra/docker/postgres-init/`. An **existing** volume doesn't run that, and a service fails to start without its database. So first start Postgres if it isn't running (the same compose command with `up -d postgres`), then run `docker exec -i macrohub-postgres sh -s < macro-backend/macro-infra/docker/postgres-init/01-service-databases.sh` (it can be re-run; add `-e ELECTION_DB_PASSWORD=...` or another service's variable after `-i` if you changed a password). A new database has no data until a scrape: see "Backend" below. **Keep an existing volume's `parliament` database: it is the only copy of its data.** Keep `macro_series` too: FRED serves the Dow Jones and S&P 500 for a rolling 10 years, so a re-scrape loses older days the database still has. A volume that still runs the old monolith needs the [migration guide's last version](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md) instead. `POSTGRES_USER` (`macrouser`) is the image's bootstrap superuser, for that script and maintenance: no service logs in with it.

**For working on the code**, run a service with `mvnw` and the frontend with Vite. Vite proxies each route to its service on `localhost` (8082 to 8085), so the start order doesn't matter:

1. **Postgres** (from repo root): `docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up -d`. It listens on `localhost:5432`. **It can't run next to the full stack's Postgres as a second one:** both Compose files define the same `postgres` service (container `macrohub-postgres`, port 5432, Compose project `docker`, volume `docker_pgdata`). With the full stack up, this command leaves its Postgres running and warns about orphan containers (ignore it, and don't pass `--remove-orphans`), and you share that database, so only stop the stack's containers that hold 8082, 8083, 8084 or 8085. If `.env` sets `REGIONAL_STATISTICS_DB_PASSWORD`, `MACRO_SERIES_DB_PASSWORD`, `PARLIAMENT_DB_PASSWORD` or `ELECTION_DB_PASSWORD`, pass the same `--env-file`, or Compose re-creates the container (on the same volume). A service that is running then answers 500 for a few seconds and reconnects by itself: see "Database". Anything else on port 5432 makes it fail to start.
2. **The service you're working on** (from `macro-backend/`), see below. `-am` builds `macro-platform` with it; only the application module actually runs (`spring-boot.run.skip` in the poms).
3. **Frontend** (from `macro-frontend/`): `PATH=~/.nvm/versions/node/v22.22.1/bin:$PATH npm run dev`. Open `http://localhost:5173`.

**To work on `regional-statistics-service`** (from `macro-backend/`, with the Compose Postgres on 5432; stop the full stack's service container first if it holds 8082): `JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl regional-statistics-service -am spring-boot:run`. It needs no profile and no `.env`. Its base properties use `localhost:5432/regional_statistics`. For any other database, set `SPRING_DATASOURCE_URL` explicitly.

**To work on `macro-series-service`** (from `macro-backend/`, with the Compose Postgres on 5432; stop the full stack's service container first if it holds 8083): `JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl macro-series-service -am spring-boot:run`. It needs no profile. The working directory is `macro-series-service/`, so `.env` there is picked up, which only a Riksbanken or Fed scrape needs. Its base properties use `localhost:5432/macro_series`. For any other database, set `SPRING_DATASOURCE_URL` explicitly.

**To work on `parliament-service`** (from `macro-backend/`, with the Compose Postgres on 5432; stop the full stack's service container first if it holds 8084): `JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl parliament-service -am spring-boot:run`. It needs no profile and no `.env`. Its base properties use `localhost:5432/parliament`. For any other database, set `SPRING_DATASOURCE_URL` explicitly.

**To work on `election-service`** (from `macro-backend/`, with the Compose Postgres on 5432; stop the full stack's service container first if it holds 8085): `JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl election-service -am spring-boot:run`. It needs no profile and no `.env`. Its base properties use `localhost:5432/election`. For any other database, set `SPRING_DATASOURCE_URL` explicitly. It reads party colours from `parliament-service` on `localhost:8084` and uses Valmyndigheten's until that answers.

### Backend (from `macro-backend/`)

```bash
# Build (each application's jar is <module>/target/<module>-1.0.0.jar, with macro-platform inside)
mvn clean package

# Run all tests of all modules (not the acceptance tests, see below)
mvn test

# Run a single test class
mvn test -Dtest=MacroSeriesControllerTest

# Run a single test method
mvn test -Dtest=MacroSeriesControllerTest#shouldReturnOk_whenValidBundleRetrieved

# Generate a service's JOOQ model, from its own database at the latest migration: the command deletes the classes of
# tables it doesn't find. The profile is named after the service, so -am leaves macro-platform's model alone. The phase
# is compile, because a service can only resolve macro-platform once that has been compiled. The default is that
# service's database on the Compose Postgres, with its role. For any other database, add -Djooq.codegen.url=...
mvn -P jooq-codegen-regional-statistics compile -pl regional-statistics-service -am
mvn -P jooq-codegen-macro-series compile -pl macro-series-service -am
mvn -P jooq-codegen-parliament compile -pl parliament-service -am
mvn -P jooq-codegen-election compile -pl election-service -am

# Generate macro-platform's JOOQ model (series_configuration, scrape_action_queue, flyway_schema_history). Every service's
# database has these tables; the default is localhost:5432/election with its role
mvn -P jooq-codegen-platform compile -pl macro-platform
```

```bash
# Acceptance tests: package the jars, build the :acceptance images, then run them (from anywhere; about 6 to 7 min in CI, of which the tests about 5 min)
macro-backend/macro-infra/scripts/acceptance-test.sh

# With those images built and nothing changed, only the tests (from macro-backend/)
mvn -pl acceptance-tests verify
```

A fresh database has series configs but no data points (`chart-data` returns 204). Populate everything with `macro-backend/macro-infra/scripts/scrape-all.sh [gateway]` (default gateway `http://localhost:8081`), which enqueues everything and drains each queue, per service and in parallel. For one service, `POST .../enqueue-all` followed by `POST .../scrape-from-queue` on its paths (see the table at the top).

Flyway migrations run automatically on application startup via `spring-boot-starter-flyway` (Boot 4 no longer auto-configures Flyway from `flyway-core` alone; there is no Flyway Maven plugin).

### Configuration profiles

Each service has one `application.properties`, with defaults for the Compose Postgres and its own database and role, and no profiles besides `test` (`src/test/resources/application-test.properties`, a TestContainers datasource). Without `SPRING_DATASOURCE_URL`, `localhost:5432` applies, which inside a container is nothing, so Compose sets it.

- Every service sets `macro.scrape.path-prefix` (no default: it doesn't start without one), `spring.flyway.baseline-on-migrate=true`, `spring.flyway.baseline-version=0`, `spring.web.resources.add-mappings=false`, `spring.jooq.sql-dialect=POSTGRES` and the connection pool's sizes (see "Database")
- Riksbanken and Fed scrapes require `riksbanken.prod.subscription.key` / `fed.prod.subscription.key` (env: `RIKSBANKEN_PROD_SUBSCRIPTION_KEY`, `FED_PROD_SUBSCRIPTION_KEY`). They are `macro-series-service`'s, and only it gets them
  - Locally, put them in `macro-backend/macro-series-service/.env` (copy `.env.example`; gitignored). Spring loads it via `spring.config.import=optional:file:.env[.properties]`, relative to the working directory, which is `macro-series-service/` under `spring-boot:run` (run a jar from there too). No other service reads a `.env`
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
- The dev server listens on port 5173. `vite.config.ts` proxies `/macro-analyzer` to the backends on `localhost` (8082 to 8085), split per domain the same way as the nginx routes, and answers 404 for any other path under `/macro-analyzer/`; `vite preview` (port 4173) uses the same proxy. A `.env.development` can't set these: the pre-commit hook and CI reject it. Backend CORS still allows all origins, for a build with `VITE_API_URL` set. That URL must be a gateway (nginx or Vite), not a backend: each service only answers its own routes
- Package manager is npm; `package-lock.json` is the only lockfile (don't add `bun.lockb`/`yarn.lock`)

### Docker (from repo root)

```bash
# Dependencies only (PostgreSQL on localhost:5432, the same service as the full stack's, so not at the same time as a second one)
docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up

# Full stack (PostgreSQL + regional-statistics-service on :8082 + macro-series-service on :8083 + parliament-service on :8084 + election-service on :8085 + frontend on :8081) — build the backend images first:
#   (cd macro-backend && ./mvnw clean package && docker build -t regional-statistics-service regional-statistics-service && docker build -t macro-series-service macro-series-service && docker build -t parliament-service parliament-service && docker build -t election-service election-service)
# The frontend image is built by compose (add --build after frontend changes)
docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up
```

- The frontend container (`macro-frontend/Dockerfile`) builds with Node 22 and serves `dist/` with nginx on 8081; `nginx/default.conf.template` falls back to `index.html` for `BrowserRouter` routes
- The image is built with `VITE_API_URL` empty (build arg, override with `--build-arg VITE_API_URL=...`, which must be a gateway, because no single backend serves every route), so the browser calls the backend on the frontend's own origin and nginx proxies it per domain, each route with its own variable (`scheme://host:port`, no trailing slash; the defaults are the compose services):
  - `MACRO_SERIES_URL` (`http://macro-series:8083`): `/macro-analyzer/series-config`, `/macro-analyzer/chart-data`, `/macro-analyzer/scrape/macro-series/`
  - `REGIONAL_STATISTICS_URL` (`http://regional-statistics:8082`): `/macro-analyzer/regional-statistics/`, `/macro-analyzer/scrape/regional-statistics/`
  - `PARLIAMENT_URL` (`http://parliament:8084`): `/macro-analyzer/parliament/`, `/macro-analyzer/scrape/parliament/`
  - `ELECTION_URL` (`http://election:8085`): `/macro-analyzer/municipalities/`, `/macro-analyzer/regions/`, `/macro-analyzer/constituencies/`, `/macro-analyzer/scrape/election/`
  - Any other path under `/macro-analyzer/`, and `/macro-analyzer` itself, answers 404 for every method. Without that it would fall through to `location /` and get `index.html` with a 200. The exception is a routed prefix without its slash, such as `/macro-analyzer/municipalities` or `/macro-analyzer/scrape/election`: nginx's automatic redirect answers those with a 301
- A new variable needs a default in the Dockerfile, because the entrypoint leaves an unset one as the literal `${X}`. A variable set to an empty value makes nginx exit at startup (`invalid number of arguments in "set" directive`), so write literal values in Compose rather than `${X:-}`. The proxy settings, including the 30-minute `proxy_read_timeout`, are set once on the `server` and inherited by every route
- The nginx image's envsubst entrypoint renders the template; each upstream is resolved per request with the resolver from `/etc/resolv.conf` (`NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1`), so nginx starts before the backend is up. nginx's resolver ignores resolv.conf search domains, so outside compose give the variables a fully qualified name

Docker Compose is the only way to run the app. There is no Kubernetes deployment: [ADR 0007](docs/adrs/0007-remove-the-kubernetes-deployment-until-its-needed.md) says why, and holds the findings worth keeping.

## Architecture

The backend is four services and one library, split by domain, plus the `acceptance-tests` module, which holds no production code. Inside each module it follows **Hexagonal Architecture (Ports & Adapters)** with **Domain-Driven Design**. Every class lives in a module under `com.kg.macroanalyzer`, except the application classes in the root package and the generated jOOQ models. The Maven modules map onto the packages: `scrape`, `shared` and their jOOQ model are built in `macro-platform`, `regionalstatistics` in `regional-statistics-service`, `macroseries` in `macro-series-service`, `parliament` in `parliament-service` and `election` in `election-service`. Each application class is in `com.kg.macroanalyzer`, so its component scanning finds `macro-platform`'s beans. [`macro-backend/ARCHITECTURE.md`](macro-backend/ARCHITECTURE.md) has more diagrams.

[`docs/architecture.md`](docs/architecture.md) is a diagram of the components that run.

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
- **`election/`** (Maven module `election-service`, with its own jOOQ model in `election.jooq.generated`) — Valmyndigheten kommun (KF), region (RF) and Riksdag valkrets (RD) elections:
  - `domain/` — `LocalElectionType`, `LocalElectionResult`, `LocalPartyResult`, `ElectedMember`, `Governance`, `ElectionScrapeFlows` (the `LOCAL_ELECTION` flow id)
  - `application/port/in/` — `ElectionQueries` (results per type and year, one area's detail) and its DTOs; `application/port/out/` — `ElectionRepositoryPort`, `PartyColorPort`, the scrape payload `ConfigWithLocalElections`, `LocalElectionIndexRequest` and `LocalElectionFile`
  - `application/service/` — `LocalElectionService` (party colours come from `PartyColorPort`), and in `scrape/` `ExistingLocalElectionFinder`, `LocalElectionScraper` and `LocalElectionPersister`
  - `adaptors/http/` — `ElectionController` (`/municipalities/*`, `/regions/*`, `/constituencies/*`); `adaptors/postgres/` — `ElectionPostgresAdaptor` and `LocalElectionRepository`; `adaptors/web/` — `RefreshingPartyColorAdaptor` and `ParliamentPartiesClient` (party colours over REST), the Valmyndigheten index and result web adaptors, retry, connection builder and parsers; `adaptors/config/` — the Valmyndigheten web beans and the scrape flow
- **`shared/`** (Maven module `macro-platform`) — used by every module, depends on none of them: `config/` (CORS, Jackson, Flyway catalog version) and `web/` (`WebPort`, `ScrapeException`, `WebAdaptorFlow`, `ConnectionBuilder`/`Getter`/`ResponseParser`, `HttpGetter`, `ZipEntryGetter`, `HttpResponses`)
  - Jackson 3 (`tools.jackson`) is used throughout. `JacksonConfiguration` (in `shared/config/`) defines the `JsonMapper` bean, which Boot's MVC auto-config uses for the message converter; don't add `@EnableWebMvc`, which would turn that auto-config off. The bean keeps `WRITE_DATES_AS_TIMESTAMPS` enabled because the frontend expects date labels as arrays (`[1994,6]`).
  - The backend is JSON-only, so `spring.web.resources.add-mappings=false` (each service's `application.properties`) turns off Boot's `/**` and `/webjars/**` resource handlers: nothing on a static location is served, and unknown paths answer 404 for every method, also CORS requests (the handler would answer them with 200 or 403). `NoStaticResourceHandlersHttpTest` pins this
- **`platform/jooq/generated/`** (`macro-platform`) and **`<module>/jooq/generated/`** (each service) — Auto-generated JOOQ classes (do not edit manually). `macro-platform`'s model holds `series_configuration`, `scrape_action_queue` and `flyway_schema_history`, for `scrape` and `shared`; each service's holds its other tables. The tables are selected in each module's codegen profile

### Layers inside a module

- `domain/` — records and enums of the domain (Lombok only, no Spring).
- `application/port/in/` — what the module offers: query interfaces (`*Queries`, `InPort`) and their DTOs/parameters.
- `application/port/out/` — what the module needs: repository ports (`*RepositoryPort`) and the request/config-with-data records its scrapers pass around.
- `application/service/` — implementations of the in-ports; `application/service/scrape/` holds the module's `ExistingFinder`/`Scraper`/`Persister`.
- `adaptors/http/` (controllers), `adaptors/postgres/` (`*PostgresAdaptor` implementing the repository port, plus jOOQ repositories), `adaptors/web/` (`WebPort` implementations, connection builders, response parsers), `adaptors/config/` (`*WebAdaptorConfiguration` for the `WebPort` beans, `*ScrapeFlowConfiguration` for the scrape flow), `adaptors/cron/` (scrape only).

### How a scrape flow plugs in

Each domain's `*ScrapeFlowConfiguration` builds a `ScrapeFlow` from the shared `Preparer` and its own finder, scraper and persister, and registers it as a `RegisteredScrapeFlow` bean under a flow id. The ids are plain strings that each module owns as constants in its `domain/` (`MacroSeriesScrapeFlows.MACRO_POINT`, `ParliamentScrapeFlows.MEMBER_OF_PARLIAMENT`/`MEMBER_ACTIVITY`, `ElectionScrapeFlows.LOCAL_ELECTION`, `RegionalStatisticsScrapeFlows.SCB_REGIONAL`); `scrape` never names them. Every `series_configuration` row says which flow scrapes it in `scrape_flow` (NOT NULL, V31). `ScrapeService` routes a queue item by name → `SeriesConfig` (through the `SeriesConfigPreparer`) → `scrapeFlow` → flow; an item whose name matches no config is `FAILED` with a warning and stays pending. At startup it fails if two flows share an id or a config's `scrapeFlow` has no registered flow (the message names the config and the id), and only warns for a registered flow that no config uses. The configs are read once at startup, so a new row needs a restart. Persisters write through their module's repository port and then call `ScrapeQueuePort.markAsDone` (with nothing to write they only mark it done). The two don't share a transaction. An item that throws is `FAILED`, doesn't stop the others and stays pending, so the cron retries it about once a minute, re-fetching from the source. The election flow can also write what it got and still leave its item `FAILED` and pending: see "Kommun, region and Riksdag elections".

### Dependency rules (`ArchitectureTest`)

Every Maven module except `acceptance-tests`, which has no production code, has its own `src/test/java/com/kg/macroanalyzer/ArchitectureTest.java` (ArchUnit, part of `mvn test`).

`macro-platform`'s enforces:

- every class resides in `scrape`, `shared` or `platform.jooq.generated`;
- `shared` depends on no module (only on itself and the platform's jOOQ model), and `scrape` only on itself, `shared` and that model;
- `scrape`'s `domain` and `application` do not depend on its `adaptors`, and no `application.port` depends on an `application.service`.

Each service's enforces the same layers for its one domain module, on top of that:

- every class except its application class resides in its module, `scrape`, `shared` or `platform.jooq.generated`;
- the module only depends on itself, `scrape` and `shared`, and `macro-platform` doesn't depend on it. `platform.jooq.generated` isn't among those, so a module can't use the queue and config tables directly;
- only its `adaptors.postgres` use its jOOQ model.

Maven keeps the services' code apart: no service depends on another. The one runtime link is over HTTP: election reads the parliament service's party list (see "Kommun, region and Riksdag elections").

### Adding a new domain

A new domain is a new service. Follow [`docs/adding-a-service.md`](docs/adding-a-service.md) for the module, database, role, port, gateway route, Compose service and tests, with `parliament-service` or `election-service` as the template. Inside the module:

1. Create `com.kg.macroanalyzer.<module>` with `domain/`, `application/port/in/`, `application/port/out/`, `application/service/` (and `service/scrape/`) and `adaptors/`, and an application class in `com.kg.macroanalyzer` so component scanning also finds `macro-platform`'s beans.
2. Define the in-port (`*Queries`) and out-port (`*RepositoryPort`); implement them in a service and a `*PostgresAdaptor`.
3. Add a controller in `adaptors/http/` that only uses the in-port.
4. Add a `*WebAdaptorConfiguration` for the `WebPort` beans and a `*ScrapeFlowConfiguration` that registers a `RegisteredScrapeFlow` under a new flow id.
5. Define that flow id as a constant in the module's `domain/` (e.g. `<Module>ScrapeFlows`). `/series-config` only lists the `macro_series` database's configs, so a new flow stays out of the chart picker.
6. Set `macro.scrape.path-prefix` (a service without it doesn't start), `spring.flyway.baseline-on-migrate=true`, `spring.flyway.baseline-version=0`, `spring.web.resources.add-mappings=false`, `spring.jooq.sql-dialect=POSTGRES` and the three `spring.datasource.hikari.*` lines (see "Database") in its `application.properties`, and give it its own `ArchitectureTest` and a copy of `ConnectionPoolIntegrationTest`. Without the `hikari` lines it still starts, on Hikari's default of 10 connections, all kept open, so count it when adding up `max_connections`.
7. Add a Flyway `V1` with the module's tables and its `series_configuration` row, then generate its jOOQ model. The row needs a `scrape_url`, a `category` and its `scrape_flow` (the id from step 5); startup fails if no flow is registered under that id.

### REST API Endpoints

- `GET /macro-analyzer/series-config` — on `macro-series-service`: list the chart series configurations, in `id` (seed) order
- `POST /macro-analyzer/chart-data` — on `macro-series-service`: retrieve aligned chart data (400 when `strategy`, `timeFrame` or `chartSeriesParams` is missing)
- `POST /macro-analyzer/scrape/{regional-statistics,macro-series,parliament,election}/enqueue?name={series}` — on that service: enqueue one of its series for scraping (202; a name it doesn't know also answers 202 and does nothing)
- `POST /macro-analyzer/scrape/{service}/enqueue-all` — enqueue all of that service's series (202)
- `POST /macro-analyzer/scrape/{service}/scrape-from-queue` — execute that service's pending scrapes (200, 409 while one is running)
- Any other path under `/macro-analyzer/` answers 404 on the gateway, for every method (a routed prefix without its slash gets nginx's 301 to the slash)
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

- All of this runs in `parliament-service`, on its own database and scrape lock. `MembersOfParliament` scrape upserts every person from `data.riksdagen.se/personlista` (JSONB, keyed on `intressent_id`); `MembersOfParliamentActivity` then fetches voting totals, speeches and motions per member of the latest two terms (584 members today, about 9 min with the members' scrape, throttled)
- Election seat counts are curated in `riksdag_election_result` (Flyway seed from Valmyndigheten) because Riksdagen only stores each person's *current* party. Add a new election as a migration in `parliament-service` (rows in `riksdag_election` + `riksdag_election_result`). `/parliament/parties` has no `ORDER BY`, so it answers in the table's order, which the baseline keeps as it was before the split (SD last)
- A re-scrape doesn't replace a local database: activity is only fetched for the latest two terms, and `riksdag_*` is curated. Nothing else holds a copy, so keep the local database rather than re-creating it
- Riksdagen configs (category `Riksdagen`, flows `MEMBER_OF_PARLIAMENT` and `MEMBER_ACTIVITY`) are in `parliament-service`'s database, so they aren't in `/series-config`, which `macro-series-service` serves from its own, and don't appear in the chart picker

### Kommun, region and Riksdag elections (Valmyndigheten)

- All of this runs in `election-service`, on its own database and scrape lock. `MunicipalElections` (KF) and `RegionElections` (RF) scrape the final results from `resultat.val.se`: `index.md5` lists one zip per kommun/region, and only the `mandatfordelning` JSON inside each is parsed (votes, seats, elected members). First run downloads ~310 zips (~1.5 min); later runs skip files whose md5 is unchanged, so corrected files are picked up automatically
- A file that fails, also on valid JSON of an unexpected shape, doesn't stop the others. What succeeded is written, but the item is `FAILED` and stays pending, so the cron retries about once a minute and only re-fetches the failed files, whose md5s aren't stored; a permanently broken file retries until it is fixed. After 5 consecutive failed files the run stops fetching and is handled the same way, so an outage doesn't hold `scrapeLock` for hours; 5 permanently broken files in a row also keep the files after them from being fetched. With nothing fetched from any changed file, nothing is written and the item is `FAILED` too. An interrupted thread abandons the run the same way and drops what it had fetched. An index without files only logs a warning and the item is marked done, because a new election's folder has no `slutlig` files until the results are final
- `ParliamentElections` (RD) uses the same pipeline, but its single zip (`s/rd/..._00_RD.zip`) holds all 29 valkretsar under `valkretsLista`, so the parser returns one area per valkrets (area code `01`..`29`, Gotland is `09`). Valkrets seats include the utjämningsmandat assigned to that valkrets (they sum to 349). The ~200 MB per-district `rostfordelning` file in the zip is streamed past, not parsed
- `scrape_url` (`.../resultatfiler/val2026`) selects the election; point it at the next election's folder with a migration. Gotland has no regionfullmäktige
- Who governs (`local_governance`) has no open data source and starts empty; add rows by migration (party abbreviations as in the results). The frontend only shows the "styre" section where a row exists
- Configs use category `Valmyndigheten` and flow `LOCAL_ELECTION`. They are in `election`'s database, so they are not in `/series-config` either
- Riksdag parties get the colours of parliament's `GET /macro-analyzer/parliament/parties` ([ADR 0005](docs/adrs/0005-cached-rest-between-services-and-no-message-broker.md)); other parties keep Valmyndigheten's colour, then gray. `RefreshingPartyColorAdaptor` (behind `PartyColorPort`) holds the list in memory, so a request never calls parliament. `ParliamentPartiesClient` fetches it on the adaptor's own daemon thread, independent of `scheduler.enabled`: first when the application is ready, then every `election.parliament.refresh-interval` (1h), or every `retry-interval` (15s) until a first list has loaded. `timeout` (5s) covers the whole exchange. A failed refresh, a 204 or an empty list keeps the list held; with no list, Valmyndigheten's colours are used. `election.parliament.url` (env `ELECTION_PARLIAMENT_URL`) defaults to `parliament-service` under `mvnw`, `http://localhost:8084`; Compose sets `http://parliament:8084`. `election-service` has no `depends_on` on parliament and starts without it. With no list yet, the colours load at the next retry after parliament is up, at most 15 seconds later
- `macro-backend/contracts/parliament-parties.json` is the contract for that endpoint: the fields election reads. `ParliamentPartiesClientTest` and `PartiesContractHttpTest` hold each side to it, using only the file and HTTP. Changing `code` or `color` needs care, because `/parliament/parties` is an API between services

### SCB regional statistics (DeSO / RegSO)

- All of this runs in `regional-statistics-service`, on its own database and scrape lock. `ScbRegionalStatistics` (category `SCB`, flow `SCB_REGIONAL`) scrapes curated indicators from SCB's PxWeb API v2 (`scrape_url` `https://statistikdatabasen.scb.se/api/v2`, CC0). Indicators live in `scb_indicator` (seeded by the service's `V1`): `numerator`/`denominator` are JSONB lists of `{table, contents, select}`; every table dimension except `Region` and `Tid` must be selected, codes within a dimension are summed, a later source replaces an earlier one per year and version, and with a denominator the value is `sum(num) / sum(den) * multiplier`
- One SCB table holds every area version; the region code tells them apart: `DeSO2018` (`0114A0010`), `DeSO2025` (`0114A0010_DeSO2025`), `RegSO2020` (`0114R001`), `RegSO2025` (`0114R001_RegSO2025`). Data is requested per version with a PxWeb wildcard (`?????????`, `*_DeSO2025`, …) and chunked by year to stay below 150 000 cells per call. Years a version has no data for are 0/null placeholders in SCB; a (version, year) is only kept when a value is non-null and non-zero. `area_code` is stored without the suffix so it matches the boundary files
- `scb_table_state` keeps SCB's `updated` stamp per table; an indicator is skipped when all its tables are unchanged and it already has rows. Stamps are written once every indicator of the run is stored; a table whose fetch failed, and every table of an indicator whose write failed, loses its stamp so the indicators using it are fetched again. To change an existing indicator's selection, also delete its `scb_area_value` and `scb_availability` rows in the migration so it is re-fetched (whether an indicator has data is read from `scb_availability`, which the persister keeps in step with the values)
- Calls are spaced by `scb.delay-ms` (default 350 ms, SCB allows 30 calls per 10 s) and retried up to 8 times: some SCB servers intermittently answer "Non-existent table" (404/400) for tables that exist. A first full scrape takes 9 to 12 minutes (2.4 M rows, ~370 MB; 8 min 49 s when last measured) because SCB spends several seconds on each uncached DeSO request; a run with unchanged tables takes about 15 to 30 seconds
- A full scrape keeps every fetched value in memory until the persister runs (~2.4 M values plus the per-source maps; the container peaked at 846 MiB in a measured first scrape, on a JVM free to use a quarter of 31 GiB, so that's an upper bound rather than what it needs). Under Compose the JVM has no `-Xmx` and the container no memory limit; if either is added, leave room for this or the first full scrape can run out of memory. Under a 1536 MiB container limit, on the since-removed Kubernetes deployment, that scrape peaked at 1348 MiB with the Serial collector, which the JVM picks by itself below 1792 MiB ([ADR 0007](docs/adrs/0007-remove-the-kubernetes-deployment-until-its-needed.md))

### Database

PostgreSQL 16, one database per service on one instance. Each service runs Flyway twice on start-up: `macro-platform`'s `db/platform` (`series_configuration`, `scrape_action_queue`, history table `flyway_platform_schema_history`), which `PlatformFlywayConfiguration` always runs first, then its own `db/migration`, whose `V1` is a squashed baseline with its config rows (a baseline at version 0, set in each service's properties, lets it run on the non-empty schema; without it the start-up fails with "Found non-empty schema(s)"). JOOQ generates type-safe table access, one model per Maven module. Every database has its own `series_configuration` and `scrape_action_queue`. The other tables: in `election`, `local_election_area`/`local_election_party_result`/`local_governance`; in `parliament`, `swedish_members_of_parliament` (JSONB), `swedish_member_activity`, `riksdag_party`/`riksdag_election`/`riksdag_election_result`/`riksdag_organ`; in `regional_statistics`, `scb_indicator`/`scb_area_value`/`scb_availability`/`scb_table_state`; in `macro_series`, the 41 rate, bond, bill, exchange-rate, Fed and Eurostat tables.

The 41 tables in `macro_series` hold one row per series and date: `UNIQUE (date)`, or `(series_name, date)` in `fed_series` and `eurostat_national_debt_series`, from `macro-series-service`'s V2 (the column is `usd_sek_date` in `exchange_usd_sek` and `policy_rate_date` in `policy_rate_sweden`). `MacroPointScraper` keeps the scraped points whose value and date aren't both stored, and `MacroPointUpsert` writes them with `ON CONFLICT ... DO UPDATE`: a revised value replaces the old one and sets `updated`, while `id`, `global_id` and `created` stay. A write of a value already stored changes nothing, so a repeated or retried scrape, or two at once, can't add rows or rewrite them. A new chart series table needs the same constraint and writer.

Chart series in `macro_series`' `series_configuration` carry picker metadata, returned by `/series-config`: `category` (`Räntor`, `Statsobligationer`, `Statsskuld`, `Valuta`, `Nyckeltal USA`), `subcategory` (e.g. `Styrränta`, `Euromarknadsränta`, `Statsskuldväxel`, `Nominell`), `unit` (short Swedish display form taken from the source: `%`, `% av BNP`, `mn EUR`, `mn USD`, `mdr USD`, `SEK/USD`, `punkter`, `index`), `country` (Swedish name, null when not country-specific) and `maturity_months` (rates/bonds/bills only). A new chart series must set them, and `scrape_flow = 'MACRO_POINT'`, in its migration; the chart series are exactly the `MACRO_POINT` configs. `ChartSeriesConfigIntegrationTest` checks that every chart series has a unit and subcategory, and lists the exact category set and row count. `ElectionApplicationTests` checks the flow of the election configs, and `ParliamentApplicationTests` of the Riksdagen ones. `/series-config` lists the configs in `id` order, and `/chart-data` gives a series its colour from its place in that order rather than the request's, so the baselines keep the ids the rows had before the split.

Every service uses Spring Boot's auto-configured HikariCP pool and jOOQ `DSLContext` (`spring.jooq.sql-dialect=POSTGRES` in its properties), and there is no connection code of its own. jOOQ's transactions run through Spring's transaction manager, on one connection per transaction, and a read on another thread doesn't wait for one or see its uncommitted changes. A SQL error from a statement arrives as Spring's `DataAccessException` (e.g. `DuplicateKeyException`), not jOOQ's, but that isn't the only kind: a `transaction` that can't get a connection throws `CannotCreateTransactionException`, which is a `TransactionException`, and a checked exception inside a transaction arrives as jOOQ's `DataAccessException`. So code that must survive an outage catches `RuntimeException`, as `ScrapeService` and `ScbRegionalPersister` do.

- **Sizes:** at most 8 connections per instance, 2 when idle. Four services at 8 is 32 of Postgres's 100 `max_connections`, which leaves room for a second replica each and a rolling update; add up again before adding replicas. Flyway uses 2 or 3 of them at start-up
- **When Postgres restarts or is re-created,** a service recovers without a restart. Requests in flight answer 500, as do requests that wait out `connection-timeout` (5 s) while it is away. Measured on a Compose stack: back within 10 s of Postgres accepting connections, which is about the JVM's 10 s cache of a failed DNS lookup, as Compose's DNS doesn't know a stopped container's name. A cron run that fails tries again a minute later, and a queue item that failed stays pending, so the next drain retries it. An interrupted `transaction` rolls back, so an SCB indicator keeps its old values. An SCB run that wrote at least one indicator is marked as done, though: a failed indicator loses its table stamps and is fetched again on the next enqueue, normally the 22:00 cron, not on the next drain
- **What this doesn't cover:** a Postgres on another host that vanishes without closing its connections. Idle connections are tested when borrowed, but with no `socketTimeout` a running query hangs. During a scrape, `ScrapeService`'s `scrapeLock` then stays held, so that instance's scrapes stop until it is restarted
- **A long write isn't cut off:** `connection-timeout` is only for getting a connection, and `max-lifetime` (default 30 min) waits until a connection is back in the pool. A `socketTimeout` below the longest SCB write would cut that write off, which is why there is none
- **At start-up nothing changes:** a service whose database can't be reached fails to start, because Flyway's first connection fails (`initialization-fail-timeout`'s default) and there are no retries. Compose's `depends_on` waits for Postgres

## Testing

- JUnit 5 + Mockito for unit tests
- TestContainers (PostgreSQL) for integration tests — Docker must be running for `mvn test`. `application-test.properties` uses `jdbc:tc:...?TC_DAEMON=true`: every context in a JVM shares that one container, and without `TC_DAEMON` it stops when the pools happen to hold no connection
- `ConnectionPoolIntegrationTest` (one in each service, on its own container) pins the dialect and pool sizes, that the service works again after `pg_terminate_backend` on all its connections, that a concurrent read neither waits for nor sees an open transaction, and that a start-up with an unreachable database fails
- Each Maven module has its own tests. `macro-platform` has the unit tests of `scrape` and `shared`, and its own `ArchitectureTest`. `SeriesConfigRepositoryTest` is in `scrape`'s package but lives in `election-service`, because it needs a running application with migrations; `NoStaticResourceHandlersHttpTest` and `ManualScrapePathsHttpTest` are in each service for the same reason. `TestSeriesConfigFactory` and `TestJsonReader` exist in several modules: a shared test-jar would be on the classpath as a jar, and `ArchitectureTest`'s `DoNotIncludeTests` would then import its classes as production code
- `ArchitectureTest` (ArchUnit, one in each Maven module with production code). `macro-platform`'s holds the rules of `scrape` and `shared`. Each service's imports all of `com.kg.macroanalyzer` from the classpath, so it sees `macro-platform` too. In all of them `importIncludesMacroPlatform` fails if the import stops finding its classes, because the rules would then pass silently. See "Dependency rules" below
- Test fixtures in `src/test/resources/json/`
- `@ActiveProfiles("test")` activates `application-test.properties`

### Acceptance tests

`macro-backend/acceptance-tests` ([ADR 0008](docs/adrs/0008-black-box-acceptance-tests-through-the-gateway.md)) starts the packaged system and only talks HTTP through the gateway. Tests are named `*AcceptanceTest` and failsafe runs them in `verify`. Surefire is off in this module, because its default includes match that name too, so `mvn test` doesn't start the stack. Docker must be running.

- `AcceptanceStack` runs everything with Testcontainers `GenericContainer`s on their own network: `postgres:16` with the real `postgres-init/`, the services (all four of `BackendService.ALL`, started in parallel), WireMock as `sources`, and the frontend's nginx as the gateway. `AcceptanceStack.running()` is the one stack per JVM that most journeys share. A journey that needs clean or unusual state starts its own with `AcceptanceStack.startWith(services)`, as `FreshSystemAcceptanceTest`, `AbsentServiceAcceptanceTest`, `ParliamentAcceptanceTest`, `ElectionAcceptanceTest`, `PartyColoursAcceptanceTest`, `RegionalStatisticsAcceptanceTest` and `FailureAndRecoveryAcceptanceTest` do, which adds up to a minute each. `BackendService.withEnvironment(...)` gives a service in such a stack extra environment variables. In a stack without a service, that service's routes answer 502 until `stack.startLater(service)` has started it under its network alias: nginx resolves an upstream per request, with `valid=10s`, so poll the first request. Ports are random and nothing has a volume or a `container_name`, so it can run next to the Compose stack and never touches `docker_pgdata`
- It reuses no Compose file, so a setting changed in `docker-compose-all.yml` isn't seen here. The `compose-smoke` CI job covers that file, see below
- Each service starts twice: once for Flyway to seed `series_configuration`, then again after `psql` in the Postgres container has pointed every `scrape_url` at `http://sources:8080`, since the configs are read once at start-up. Setup fails if a row points elsewhere, so a scrape can't reach a real source. That is the only time the harness touches the database, and the module has no JDBC driver, so a test can't. `SCHEDULER_ENABLED=false` keeps the crons out
- The images are `regional-statistics-service:acceptance`, `macro-series-service:acceptance`, `parliament-service:acceptance`, `election-service:acceptance` and `macro-frontend:acceptance` (`-Dacceptance.image.macro-series=...`, the same for `regional-statistics`, `parliament` and `election`, and `-Dacceptance.image.gateway=...` to use others), so a run doesn't change what a local Compose stack starts. `macro-infra/scripts/acceptance-test.sh` builds them each time, and `mvn -pl acceptance-tests verify` runs against whatever is there, so after a code change use the script. A missing image fails with a message naming the script
- Under snap's `docker`, whose private `/tmp` hides a checkout there from `docker build`, the script fails with `path "macro-series-service" not found`. Run it from a clone under `$HOME`
- A journey is a test class that uses `AcceptanceStack.running().gateway()` and `.sources()`: `sources.serveJson(path, fixture)` with a fixture from `src/test/resources/sources/`, requests through the gateway, then exact values in the answer. `sources.requestCount(path)` shows the stub was hit. A source whose request is told apart by its query, such as Fed or Eurostat, uses the overloads with a query map, `sources.serveJson(path, query, fixture)` and `sources.requestCount(path, query)`, because a stub that ignores the query would hide a wrong URL: with them, a wrong URL gets the stub's 404 and the chart answers 204. `sources.serveJsonBody(path, body)`, also with a query map, serves a body built in the test, such as dates computed from today. In the shared stack each journey uses its own series or area, because a failed queue item stays pending and every later drain retries it, and asserts nothing that another journey's scrape would change. Parliament has one list of members and no series or area to keep apart, so its member journeys own their stacks, and on the shared stack nothing but the parties and elections is read from it. Election's lists answer for every area and all three types read one `index.md5`, so its journeys own their stacks too: `Valmyndigheten.publish(sources, files...)` serves the zips, built in the test from a `mandatfordelning` fixture with `Valmyndigheten.ResultFile`, and an index with their real md5s, through `sources.serveBytes` and `sources.serveText`. Colours of Riksdag parties aren't asserted there, since they depend on parliament's list. `PartyColoursAcceptanceTest` does that, with parliament running, absent and started late: election fetches the list in the background, so it polls with `Eventually.assertWithin` and sets `ELECTION_PARLIAMENT_RETRYINTERVAL` to 1 s. A scrape walks every SCB indicator, and a table that answers an error costs 25 s of retries, so `RegionalStatisticsAcceptanceTest` owns its stack as well and only serves `households`' table: `sources.serveJsonForOtherPathsMatching(regex, fixture)`, below the default stub priority, answers every other table's metadata without the dimensions its indicators select, which fails them at once. That table has no other indicator, so those failures don't drop its stamp. A data stub matches the region wildcard of one boundary version, and `SCB_DELAYMS=0` turns the throttle off. `FailureAndRecoveryAcceptanceTest` uses `sources.failWith(path, status)` and `sources.serveJsonDelayed(path, fixture, delay)`, and doesn't assert the answer of a drain whose items fail, which is 200 regardless. It holds a scrape open with the delay and polls `sources.requestCount` to know that it has started, as WireMock logs a request before delaying its answer. `stack.stopPostgres()` and `stack.startStoppedPostgres()` keep the container, and so the anonymous volume with the data, which `GenericContainer.stop()` would remove. Another service joins as a `BackendService` constant in `ALL` and a `docker build` line in the script
- `macro-infra/scripts/compose-smoke.sh` is the `compose-smoke` CI job, and **it only runs in CI: it refuses without `CI=true`, and also when the `docker_pgdata` volume or `macrohub-postgres` container exists, and must not be run locally**, because it would collide with a local stack and could share `docker_pgdata`. It builds the service images as `:latest` on the runner, copies `.env.example` to `.env` and starts the real `docker-compose-all.yml` on an empty volume, with `macro-infra/scripts/compose-smoke.override.yml`, which only sets `SCHEDULER_ENABLED=false` on the services, so that no cron can call a real source. The script fails before starting anything if a service lacks that. Then, within 5 minutes, each service's route has to answer through `http://localhost:8081`, with the seeded counts (108 series, 11 parties, 26 indicators), and an unknown path has to answer 404. It scrapes nothing, and the job takes about 2 minutes. A new chart series, party or indicator changes those counts, here as in `FreshSystemAcceptanceTest`

## Workflow

Agreed changes are implemented with the `/ship` skill (`.claude/skills/ship/SKILL.md`): an implementer sub-agent opens a PR, a separate reviewer sub-agent (Fable, or Opus when Fable credits run out) reviews it, then it is merged once the reviewer says LGTM. `main` is protected: every change needs a PR, and the `frontend` and `backend` jobs in `.github/workflows/ci.yml` (frontend: tracked-`.env` check, `npm run typecheck`, build, lint; backend: `mvn test`) must pass. No approval is required. The `acceptance` job (`macro-infra/scripts/acceptance-test.sh`) also runs on every PR and isn't a required check ([ADR 0008](docs/adrs/0008-black-box-acceptance-tests-through-the-gateway.md)), so read its result before merging. So does the `compose-smoke` job (`macro-infra/scripts/compose-smoke.sh`), which starts the real `docker-compose-all.yml` and isn't required either.

Larger decisions are recorded as ADRs in `docs/adrs/`. The finished [migration guide](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md), at its last version, records how the backend was split into services. To add one, follow [`docs/adding-a-service.md`](docs/adding-a-service.md).

PRs also get an automatic Claude review comment from `.github/workflows/claude-review.yml` (comment-only, not a required check; a PR that changes that file is skipped, since the action requires it to match `main`). It needs the Claude GitHub App installed on the repo and a `CLAUDE_CODE_OAUTH_TOKEN` repo secret generated with `claude setup-token` (Pro/Max subscription).

## Code comments

- No comments that describe what the code does: no restating, and no summaries of classes or methods, including Javadoc/TSDoc and section labels. Express it through names and structure.
- A comment is allowed, rarely, on complicated code to explain why it looks the way it does: a non-obvious constraint, an external system's quirk, a workaround.
- `// given`, `// when` and `// then` in tests stay.

## Commit Convention

Follows conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `config:`, `chore:`, `docs:`
