# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

macro-hub visualizes macro-economic trends from multiple data sources (Riksbanken, Eurostat, Federal Reserve, Swedish Parliament, Valmyndigheten, SCB). It consists of:

- **macro-backend/macro-analyzer/** — Java 25 / Spring Boot 4.1.2 backend
- **macro-frontend/** — Vite + React 18 + TypeScript + shadcn-ui dashboard (formerly the separate `macro-view-hub` repo, merged in via `git subtree`)
- **macro-backend/macro-infra/** — Docker Compose files (`docker/`) and microk8s manifests (`k8s/`)

## Build & Test Commands

### Toolchain requirements

- **JDK 25** for the backend (installed via SDKMAN). If `java -version` isn't 25 (e.g. an IDE exported an older `JAVA_HOME`), set `JAVA_HOME=~/.sdkman/candidates/java/current` for Maven commands.
- Lombok is registered under `annotationProcessorPaths` in the compiler plugin because JDK 23+ no longer runs annotation processors found on the classpath; without it the build fails with `cannot find symbol` errors (`log`, getters/builders).
- **Node 20.19+ or 22.12+** for the frontend (Vite 7). Node 18 fails with `crypto.hash is not a function`.
- `mvnw` and the `*.sh` scripts are committed as executable. The root `.gitattributes` keeps them (and `.githooks/*`) LF even under `core.autocrlf=true`, so `./mvnw ...` works; if an old checkout still has CRLF copies, delete them and `git checkout -- .` to refresh.
- Docker must be running for Postgres and for `mvn test` (TestContainers).

### Starting the local stack (in this order)

Both the backend and Vite want port 8080, and the frontend calls the backend at `http://localhost:8080`, so the order matters.

1. **Check what is already running:** `lsof -nP -iTCP -sTCP:LISTEN | grep -E ':(8080|8081|31274)'`. If an older backend holds 8080, it must be restarted to serve new code.
2. **Postgres** (from repo root): `docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up -d`. It listens on `localhost:31274`.
3. **Backend** (from `macro-backend/macro-analyzer/`, so `.env` is picked up): `SPRING_DATASOURCE_PASSWORD=macropassword JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`. Wait until it serves requests on 8080 (e.g. `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/macro-analyzer/series-config` returns 200) before starting Vite. Flyway migrations run on startup.
4. **Frontend** (from `macro-frontend/`): `PATH=~/.nvm/versions/node/v22.22.1/bin:$PATH npm run dev`. With 8080 taken, Vite falls back to **8081**; open `http://localhost:8081`.

If Vite starts first it takes 8080 and the backend fails with "Port 8080 was already in use".

### Backend (from `macro-backend/macro-analyzer/`)

```bash
# Build
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=MacroSeriesControllerTest

# Run a single test method
mvn test -Dtest=MacroSeriesControllerTest#shouldReturnOk_whenValidBundleRetrieved

# Generate JOOQ model from database schema
mvn -P jooq-codegen generate-sources

# Run locally against the Docker Postgres (dev profile points at localhost:31274)
SPRING_DATASOURCE_PASSWORD=macropassword ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A fresh database has series configs but no data points (`chart-data` returns 204). Populate it with `POST /macro-analyzer/enqueue-all` followed by `POST /macro-analyzer/scrape-from-queue`.

Flyway migrations run automatically on application startup via `spring-boot-starter-flyway` (Boot 4 no longer auto-configures Flyway from `flyway-core` alone; there is no Flyway Maven plugin).

### Configuration profiles

- `application.properties` — base config; datasource points at the microk8s cluster (`192.168.154.7:31274`)
- `application-dev.properties` — overrides datasource URL to `localhost:31274`
- `application-test.properties` — TestContainers datasource
- `prod` profile (used by the Dockerfile and `docker-compose-all.yml`) has no properties file; config comes from env vars
- Riksbanken and Fed scrapes require `riksbanken.prod.subscription.key` / `fed.prod.subscription.key` (env: `RIKSBANKEN_PROD_SUBSCRIPTION_KEY`, `FED_PROD_SUBSCRIPTION_KEY`)
  - Locally, put them in `macro-backend/macro-analyzer/.env` (copy `.env.example`; gitignored). Spring loads it via `spring.config.import=optional:file:.env[.properties]`, relative to the working directory, so run the app from `macro-analyzer/`
  - For `docker-compose-all.yml`, pass `--env-file macro-backend/macro-analyzer/.env`
  - `.githooks/pre-commit` rejects commits that stage `.env` files (even via `git add -f`). Enable it once per clone: `git config core.hooksPath .githooks`. The hook is opt-in, so CI's `frontend` job also fails if any `.env` file is tracked (same rule: `.env`, `.env.<anything>`, except `.env.example`)

### Frontend (from `macro-frontend/`)

```bash
npm install
npm run dev        # Vite dev server
npm run build      # production build
npm run build:dev  # development-mode build
npm run lint
npm run typecheck  # tsc --noEmit for tsconfig.app.json and tsconfig.node.json (vite build does not type-check)
npm run preview    # serve the production build
```

- Uses Recharts for charts, TanStack Query, React Router; `@/` aliases `src/`
- Kommun/region/valkrets maps (`ChoroplethMap`) render `public/geo/kommuner.topo.json`, `lan.topo.json` and `valkretsar.topo.json` with d3-geo. They are generated from SCB's CC0 boundary shapefiles by `scripts/build-geo.sh` (run from `macro-frontend/`; needs network and `npx`), and the outputs are committed. Valkretsar are dissolved from the kommun layer via `scripts/valkretsar.csv` (kommunkod → valkrets); update it if the valkrets division changes
- DeSO/RegSO boundaries for the Områdesstatistik map are in `public/geo/deso_2018.topo.json`, `deso_2025.topo.json`, `regso_2020.topo.json` and `regso_2025.topo.json` (object name = file stem; properties `code` (desokod/regsokod without version suffix), `name` (`regsonamn`, or `"<kommunnamn> <desokod>"` for DeSO), `kommunkod`, and on DeSO also `regsokod`). They are generated from SCB's WFS (`geodata.scb.se/geoserver/stat/wfs`, CC0) by `scripts/build-geo-scb-areas.sh` (run from `macro-frontend/`; needs network, `npx` and `node`, ~2 min). The script fails if a layer's feature count differs from the WFS hit count or a code is missing/duplicated. SCB's 2025 layers extend into territorial waters, so the script clips them to the dissolved land outline of DeSO 2018 / RegSO 2020 (lakes stay inside areas in every version). Simplification is Douglas-Peucker with a tolerance scaled by area (~4 m for inner-city areas, up to 150 m for rural ones) to keep the files at ~3.0 MB (DeSO) and ~1.9 MB (RegSO); `quantization=1e6` keeps vertices within ~1.5 m for street-level zoom
- The Områdesstatistik page (`/omraden`, `src/pages/RegionalStatistics.tsx`, components in `src/components/regionalmap/`) is lazy-loaded so MapLibre GL (`maplibre-gl`, pinned exact; its worker is set via `setWorkerUrl` from a `?url` import because bundling breaks the library's own lookup) stays out of the main bundle. The basemap is OpenFreeMap's `positron` style (no API key; attribution OpenFreeMap/OpenMapTiles/OpenStreetMap comes from the style, SCB is added). The values' `geoVersion` picks which boundary file is fetched (TanStack Query, cached per version). Areas are coloured in 7 quantile classes via feature-state (blue sequential; red–gray–blue diverging when values have both signs, e.g. flyttnetto). URL state: `l` (DeSO|RegSO), `i` (indicator id), `y` (year), `a` (area code); an indicator without DeSO data is shown at RegSO. Address search uses Photon (`photon.komoot.io`, bbox around Sweden, results filtered to `countrycode=SE`; Photon rejects `lang=sv`, so no lang is sent) and finds the containing area with d3-geo `geoContains`
- shadcn-ui components live in `src/components/ui/` (configured via `components.json`)
- Backend base URL is `rootUrl` in `src/models/Constants.ts`, set at build time by `VITE_API_URL` (trailing slash stripped). Unset (e.g. `npm run dev`) it defaults to `http://localhost:8080`; an empty value means same origin (relative `/macro-analyzer/...` URLs). It calls `/macro-analyzer/series-config`, `/macro-analyzer/chart-data` and the parliament, election and `regional-statistics` endpoints
- **Port clash:** `vite.config.ts` also sets the dev server to port 8080. Start the backend first so Vite falls back to the next free port (8081); if Vite starts first, Spring Boot fails to bind. Backend CORS allows all origins (the dev server calls `:8080` cross-origin).
- Package manager is npm; `package-lock.json` is the only lockfile (don't add `bun.lockb`/`yarn.lock`)

### Docker (from repo root)

```bash
# Dependencies only (PostgreSQL on localhost:31274)
docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up

# Full stack (PostgreSQL + backend on :8080 + frontend on :8081) — build the backend image first:
#   (cd macro-backend/macro-analyzer && ./mvnw clean package && docker build -t macro-analyzer .)
# The frontend image is built by compose (add --build after frontend changes)
docker compose --env-file macro-backend/macro-analyzer/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up
```

- The frontend container (`macro-frontend/Dockerfile`) builds with Node 22 and serves `dist/` with nginx; `nginx/default.conf.template` falls back to `index.html` for `BrowserRouter` routes. It replaces the Vite dev server on 8081, so stop Vite first
- The image is built with `VITE_API_URL` empty (build arg, override with `--build-arg VITE_API_URL=...`), so the browser calls the backend on the frontend's own origin and nginx proxies `/macro-analyzer/` to `BACKEND_URL` (env, `scheme://host:port`, default `http://macro-analyzer:8080`, the compose service). The nginx image's envsubst entrypoint renders the template; the upstream is resolved per request with the resolver from `/etc/resolv.conf` (`NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1`), so nginx starts before the backend is up. nginx's resolver ignores resolv.conf search domains, so outside compose (e.g. a future k8s deployment; `k8s/` has no frontend manifest today) give `BACKEND_URL` a fully qualified name such as `http://macro-analyzer-service.default.svc.cluster.local:8080`

## Architecture

The backend is split into **modules by domain**. Inside each module it follows **Hexagonal Architecture (Ports & Adapters)** with **Domain-Driven Design**. Every class lives in a module under `com.kg.macroanalyzer`, except `MacroAnalyzerApplication` in the root package and the generated jOOQ model.

```
  REST ──→ adaptors.http ──→ port.in ──→ service ──→ port.out ←── adaptors.postgres ──→ PostgreSQL
                                           │
                                           └──→ domain

  InsertionAdaptor, CronEnqueueAdaptor, CronScrapeAdaptor ──→ InPort ──→ EnqueueService ──→ ScrapeQueuePort
                                                                    └──→ ScrapeService
  ScrapeService ──→ RegisteredScrapeFlow (picked by the config's scrape_flow id, built in each module's adaptors.config)
      Preparer ──→ ExistingFinder ──→ Scraper ──→ WebPort ←── adaptors.web ──→ external API
                                      Persister ──→ port.out, ScrapeQueuePort.markAsDone

  election ──→ parliament.application.port.in (PartyDirectory), parliament.domain (Party)
```

### Package structure (`com.kg.macroanalyzer`)

The backend is organised by domain. Every module (`scrape`, `macroseries`, `regionalstatistics`, `parliament`, `election`) uses `domain/`, `application/port/in/`, `application/port/out/`, `application/service/` and `adaptors/{http,postgres,web,config,cron}/`; besides them there is only `shared/`, the generated jOOQ model and `MacroAnalyzerApplication` in the root package.

- **`scrape/`** — the scrape core, independent of the data sources:
  - `domain/` — `ScrapeQueueItem`, `ScrapeResult`, `SeriesConfig` (every scrape flow's config, including the `scrapeFlow` id that routes it)
  - `application/port/in/` — `InPort` (enqueue, enqueue-all, scrape-from-queue) and its `ScrapeRun` result; `application/port/out/` — `ScrapeQueuePort` (queue reads/writes, `markAsDone`) and `SeriesConfigPort`
  - `application/service/` — `InPortImpl`, `ScrapeService`, `EnqueueService`, and in `scrapeflow/` the `ScrapeFlow` pipeline (`Preparer` → `ExistingFinder` → `Scraper` → `Persister`) and `RegisteredScrapeFlow` (see below)
  - `adaptors/http/` — `InsertionAdaptor` (enqueue/scrape endpoints); `adaptors/cron/` — `CronScrapeAdaptor` drains the queue every 60s, `CronEnqueueAdaptor` enqueues all series every minute during the 22:00 hour; `adaptors/postgres/` — queue and series-config repositories; `adaptors/config/` — preparer, series-config list and scheduler beans
- **`macroseries/`** — chart time series (Riksbanken, Fed and Eurostat rates, bonds, bills, exchange rates and key figures):
  - `domain/` — `MacroSeries`, `MacroPoint`, `AlignedBundle`, `InvalidBundleDimensionException`, `MacroSeriesScrapeFlows` (the `MACRO_POINT` flow id)
  - `application/port/in/` — `MacroSeriesQueries` (aligned chart data, chart series configs) and its DTOs (`BuildChartDataParams` with the `Strategy` and `TimeFrame` enums, `ChartSeriesParam`, `ChartData`, `ChartDataWithLabels`); `application/port/out/` — `MacroSeriesRepositoryPort` and the scrape payload `ConfigWithMacroPoints`
  - `application/service/` — `MacroSeriesService` (the chart list is the configs of the `MACRO_POINT` flow), `LabelGenerationService`, in `bundleformat/` `TimeFilterService`, `AlignmentService` and the sample and time-filter strategies, and in `scrape/` `ExistingMacroPointFinder`, `MacroPointScraper` and `MacroPointPersister`
  - `adaptors/http/` — `MacroSeriesController` (`/series-config`, `/chart-data`), `SeriesConfigResponse` (a `/series-config` entry, without `scrapeFlow`) and `ColorSelectionStrategy`; `adaptors/postgres/` — `MacroSeriesPostgresAdaptor` and the per-table rate, bond, bill, exchange-rate, Fed and Eurostat repositories; `adaptors/web/` — `MacroPointWebAdaptor` and `MacroPointAdaptorFlowFactory`, which routes to the Riksbanken, Fed or Eurostat connection builder and response parser; `adaptors/config/` — the web adaptor bean (with the Riksbanken/Fed subscription keys) and the scrape flow
- **`regionalstatistics/`** — SCB DeSO/RegSO statistics:
  - `domain/` — `GeoVersion`, `ScbLevel`, `ScbIndicator`, `ScbValues` and the other SCB value types, `RegionalStatisticsScrapeFlows` (the `SCB_REGIONAL` flow id)
  - `application/port/in/` — `RegionalStatisticsQueries` (indicators and values) and its DTOs; `application/port/out/` — `RegionalStatisticsRepositoryPort`, the scrape payload `ConfigWithScbIndicators` and the SCB web requests
  - `application/service/` — `RegionalStatisticsService`, and in `scrape/` the scrape flow (`ExistingScbTableFinder`, `ScbRegionalScraper`, `ScbRegionalPersister`)
  - `adaptors/http/` — `RegionalStatisticsController` (`/regional-statistics/*`); `adaptors/postgres/` — `RegionalStatisticsPostgresAdaptor` and `ScbRegionalRepository`; `adaptors/web/` — SCB web adaptors, rate limiter, retry, connection builder and JSON-stat parsers; `adaptors/config/` — the SCB web beans and the scrape flow
- **`parliament/`** — Riksdagen members, activity, parties and elections:
  - `domain/` — `MemberOfParliament`, `MemberActivity`, `Party`, `Election` and the other Riksdagen value types, `ParliamentScrapeFlows` (the `MEMBER_OF_PARLIAMENT` and `MEMBER_ACTIVITY` flow ids)
  - `application/port/in/` — `ParliamentQueries` (parties, elections, composition, members) and its DTOs, and `PartyDirectory` (the `Party` records, for the election code's party colours); `application/port/out/` — `ParliamentRepositoryPort`, the scrape payloads `ConfigWithMemberOfParliament`/`ConfigWithMemberActivity` and `MemberActivityRequest`
  - `application/service/` — `ParliamentService` (implements both in-ports), and in `scrape/` the member and member-activity finders, scrapers and persisters
  - `adaptors/http/` — `ParliamentController` (`/parliament/*`); `adaptors/postgres/` — `ParliamentPostgresAdaptor`, `SweParMemberRepository`, `SweParActivityRepository` and `RiksdagElectionRepository`; `adaptors/web/` — `MembersOfParliamentWebAdaptor`, `RiksdagenActivityWebAdaptor`, the Riksdagen connection builders and response parsers; `adaptors/config/` — the Riksdagen web beans and both scrape flows
- **`election/`** — Valmyndigheten kommun (KF), region (RF) and Riksdag valkrets (RD) elections:
  - `domain/` — `LocalElectionType`, `LocalElectionResult`, `LocalPartyResult`, `ElectedMember`, `Governance`, `ElectionScrapeFlows` (the `LOCAL_ELECTION` flow id)
  - `application/port/in/` — `ElectionQueries` (results per type and year, one area's detail) and its DTOs; `application/port/out/` — `ElectionRepositoryPort`, the scrape payload `ConfigWithLocalElections`, `LocalElectionIndexRequest` and `LocalElectionFile`
  - `application/service/` — `LocalElectionService` (party colours come from parliament's `PartyDirectory`), and in `scrape/` `ExistingLocalElectionFinder`, `LocalElectionScraper` and `LocalElectionPersister`
  - `adaptors/http/` — `ElectionController` (`/municipalities/*`, `/regions/*`, `/constituencies/*`); `adaptors/postgres/` — `ElectionPostgresAdaptor` and `LocalElectionRepository`; `adaptors/web/` — the Valmyndigheten index and result web adaptors, retry, connection builder and parsers; `adaptors/config/` — the Valmyndigheten web beans and the scrape flow
- **`shared/`** — used by every module, depends on none of them: `config/` (CORS, Jackson, JDBC, Flyway catalog version) and `web/` (`WebPort`, `ScrapeException`, `WebAdaptorFlow`, `ConnectionBuilder`/`Getter`/`ResponseParser`, `HttpGetter`, `ZipEntryGetter`, `HttpResponses`)
  - Jackson 3 (`tools.jackson`) is used throughout. `CorsConfiguration` (in `shared/config/`) has `@EnableWebMvc`, which disables Boot's MVC auto-config, so `JacksonConfiguration` registers its `JsonMapper` with MVC explicitly. It keeps `WRITE_DATES_AS_TIMESTAMPS` enabled because the frontend expects date labels as arrays (`[1994,6]`).
- **`jooq/generated/`** — Auto-generated JOOQ classes (do not edit manually).

### Layers inside a module

- `domain/` — records and enums of the domain (Lombok only, no Spring).
- `application/port/in/` — what the module offers: query interfaces (`*Queries`, `InPort`, `PartyDirectory`) and their DTOs/parameters.
- `application/port/out/` — what the module needs: repository ports (`*RepositoryPort`) and the request/config-with-data records its scrapers pass around.
- `application/service/` — implementations of the in-ports; `application/service/scrape/` holds the module's `ExistingFinder`/`Scraper`/`Persister`.
- `adaptors/http/` (controllers), `adaptors/postgres/` (`*PostgresAdaptor` implementing the repository port, plus jOOQ repositories), `adaptors/web/` (`WebPort` implementations, connection builders, response parsers), `adaptors/config/` (`*WebAdaptorConfiguration` for the `WebPort` beans, `*ScrapeFlowConfiguration` for the scrape flow), `adaptors/cron/` (scrape only).

### How a scrape flow plugs in

Each domain's `*ScrapeFlowConfiguration` builds a `ScrapeFlow` from the shared `Preparer` and its own finder, scraper and persister, and registers it as a `RegisteredScrapeFlow` bean under a flow id. The ids are plain strings that each module owns as constants in its `domain/` (`MacroSeriesScrapeFlows.MACRO_POINT`, `ParliamentScrapeFlows.MEMBER_OF_PARLIAMENT`/`MEMBER_ACTIVITY`, `ElectionScrapeFlows.LOCAL_ELECTION`, `RegionalStatisticsScrapeFlows.SCB_REGIONAL`); `scrape` never names them. Every `series_configuration` row says which flow scrapes it in `scrape_flow` (NOT NULL, V31). `ScrapeService` routes a queue item by name → `SeriesConfig` (through the `SeriesConfigPreparer`) → `scrapeFlow` → flow; an item whose name matches no config is `FAILED` with a warning and stays pending. At startup it fails if two flows share an id or a config's `scrapeFlow` has no registered flow (the message names the config and the id), and only warns for a registered flow that no config uses. The configs are read once at startup, so a new row needs a restart. Persisters write through their module's repository port and call `ScrapeQueuePort.markAsDone`.

### Dependency rules (`ArchitectureTest`)

`src/test/java/com/kg/macroanalyzer/ArchitectureTest.java` (ArchUnit, part of `mvn test`) enforces:

- every class except `MacroAnalyzerApplication` resides in a module listed in `MODULES`/`SHARED_MODULES` or in `jooq.generated`;
- `shared` depends on no module;
- a module's `domain` and `application` do not depend on its `adaptors`, and no `application.port` depends on any module's `application.service` (`scrape`'s included);
- a domain module (`DOMAIN_MODULES`) only depends on itself, `scrape`, `shared` and `jooq.generated`, plus what `DOMAIN_MODULE_EXTRA_DEPENDENCIES` allows. The only entry is `election` → `parliament.application.port.in` and `parliament.domain` (for `PartyDirectory`/`Party`); no module may use another module's out-ports, services or adaptors.

### Adding a new domain

1. Create `com.kg.macroanalyzer.<module>` with `domain/`, `application/port/in/`, `application/port/out/`, `application/service/` (and `service/scrape/`) and `adaptors/`.
2. Define the in-port (`*Queries`) and out-port (`*RepositoryPort`); implement them in a service and a `*PostgresAdaptor`.
3. Add a controller in `adaptors/http/` that only uses the in-port.
4. Add a `*WebAdaptorConfiguration` for the `WebPort` beans and a `*ScrapeFlowConfiguration` that registers a `RegisteredScrapeFlow` under a new flow id.
5. Define that flow id as a constant in the module's `domain/` (e.g. `<Module>ScrapeFlows`). Only `MACRO_POINT` configs appear in `/series-config`, so a new flow stays out of the chart picker without further changes.
6. Add the module to `MODULES` and `DOMAIN_MODULES` in `ArchitectureTest` (and to `DOMAIN_MODULE_EXTRA_DEPENDENCIES` only if it must use another module's in-port).
7. Add a Flyway migration with the module's tables and its `series_configuration` row, then regenerate the jOOQ model. The row needs a `scrape_url`, a `category` and its `scrape_flow` (the id from step 5); startup fails if no flow is registered under that id.

### REST API Endpoints

- `GET /macro-analyzer/series-config` — list the chart series configurations, in `id` (seed) order
- `POST /macro-analyzer/chart-data` — retrieve aligned chart data (400 when `strategy`, `timeFrame` or `chartSeriesParams` is missing)
- `POST /macro-analyzer/enqueue?name={series}` — enqueue a series for scraping (202)
- `POST /macro-analyzer/enqueue-all` — enqueue all series (202)
- `POST /macro-analyzer/scrape-from-queue` — execute pending scrapes (200)
- `GET /macro-analyzer/parliament/elections` — official seat results per election (newest first)
- `GET /macro-analyzer/parliament/composition` — live seats per party from currently serving MPs
- `GET /macro-analyzer/parliament/parties` — party names, colours, ideology
- `GET /macro-analyzer/parliament/members?year={electionYear}` — members who sat in that term (default: latest)
- `GET /macro-analyzer/parliament/members/{intressentId}` — member detail (assignments, biography, contact, votes, speeches, motions)
- `GET /macro-analyzer/municipalities/elections?year=` / `GET /macro-analyzer/regions/elections?year=` / `GET /macro-analyzer/constituencies/elections?year=` — per kommun/region/Riksdag valkrets: seats, turnout, votes and seats per party (default: latest year)
- `GET /macro-analyzer/municipalities/{kommunkod}` / `GET /macro-analyzer/regions/{länskod}` / `GET /macro-analyzer/constituencies/{valkretskod}` — the same plus elected members and curated governance (none for valkretsar)
- `GET /macro-analyzer/regional-statistics/indicators` — SCB indicators with `availability` per level (`DeSO`, `RegSO`): years (newest first) and the boundary version (`geoVersion`) of each
- `GET /macro-analyzer/regional-statistics/values?indicator=&level=DeSO|RegSO&year=` — `{indicator, level, year, geoVersion, values: {areaCode: value}}` (default: latest year; 204 without data, 400 for an unknown level)

### Riksdagen (parliament) data

- `MembersOfParliament` scrape upserts every person from `data.riksdagen.se/personlista` (JSONB, keyed on `intressent_id`); `MembersOfParliamentActivity` then fetches voting totals, speeches and motions per member of the latest two terms (~600 members, ~7 min, throttled)
- Election seat counts are curated in `riksdag_election_result` (Flyway seed from Valmyndigheten) because Riksdagen only stores each person's *current* party. Add a new election as a migration (rows in `riksdag_election` + `riksdag_election_result`)
- Riksdagen configs (category `Riksdagen`, flows `MEMBER_OF_PARLIAMENT` and `MEMBER_ACTIVITY`) are not in `/series-config`, which only lists `MACRO_POINT` configs, so they don't appear in the chart picker

### Kommun, region and Riksdag elections (Valmyndigheten)

- `MunicipalElections` (KF) and `RegionElections` (RF) scrape the final results from `resultat.val.se`: `index.md5` lists one zip per kommun/region, and only the `mandatfordelning` JSON inside each is parsed (votes, seats, elected members). First run downloads ~310 zips (~1.5 min); later runs skip files whose md5 is unchanged, so corrected files are picked up automatically
- `ParliamentElections` (RD) uses the same pipeline, but its single zip (`s/rd/..._00_RD.zip`) holds all 29 valkretsar under `valkretsLista`, so the parser returns one area per valkrets (area code `01`..`29`, Gotland is `09`). Valkrets seats include the utjämningsmandat assigned to that valkrets (they sum to 349). The ~200 MB per-district `rostfordelning` file in the zip is streamed past, not parsed
- `scrape_url` (`.../resultatfiler/val2026`) selects the election; point it at the next election's folder with a migration. Gotland has no regionfullmäktige
- Who governs (`local_governance`) has no open data source and starts empty; add rows by migration (party abbreviations as in the results). The frontend only shows the "styre" section where a row exists
- Configs use category `Valmyndigheten` and flow `LOCAL_ELECTION`, so they are not in `/series-config` either

### SCB regional statistics (DeSO / RegSO)

- `ScbRegionalStatistics` (category `SCB`, flow `SCB_REGIONAL`, so not in `/series-config`) scrapes curated indicators from SCB's PxWeb API v2 (`scrape_url` `https://statistikdatabasen.scb.se/api/v2`, CC0). Indicators live in `scb_indicator` (seeded by V29): `numerator`/`denominator` are JSONB lists of `{table, contents, select}`; every table dimension except `Region` and `Tid` must be selected, codes within a dimension are summed, a later source replaces an earlier one per year and version, and with a denominator the value is `sum(num) / sum(den) * multiplier`
- One SCB table holds every area version; the region code tells them apart: `DeSO2018` (`0114A0010`), `DeSO2025` (`0114A0010_DeSO2025`), `RegSO2020` (`0114R001`), `RegSO2025` (`0114R001_RegSO2025`). Data is requested per version with a PxWeb wildcard (`?????????`, `*_DeSO2025`, …) and chunked by year to stay below 150 000 cells per call. Years a version has no data for are 0/null placeholders in SCB; a (version, year) is only kept when a value is non-null and non-zero. `area_code` is stored without the suffix so it matches the boundary files
- `scb_table_state` keeps SCB's `updated` stamp per table; an indicator is skipped when all its tables are unchanged and it already has rows. Stamps are written once every indicator of the run is stored; a table whose fetch failed, and every table of an indicator whose write failed, loses its stamp so the indicators using it are fetched again. To change an existing indicator's selection, also delete its `scb_area_value` and `scb_availability` rows in the migration so it is re-fetched (whether an indicator has data is read from `scb_availability`, which the persister keeps in step with the values)
- Calls are spaced by `scb.delay-ms` (default 350 ms, SCB allows 30 calls per 10 s) and retried up to 8 times: some SCB servers intermittently answer "Non-existent table" (404/400) for tables that exist. A first full scrape takes about 12 minutes (2.4 M rows, ~370 MB) because SCB spends several seconds on each uncached DeSO request; a run with unchanged tables takes about 30 seconds
- A full scrape keeps every fetched value in memory until the persister runs (~2.4 M values plus the per-source maps, a few hundred MB of heap). The JVM has no `-Xmx` and the pods no memory limit today; if either is added, leave room for this or the first full scrape can run out of memory

### Database

PostgreSQL 16 with Flyway migrations in `src/main/resources/db/migration/` (V1–V32). JOOQ generates type-safe table access. Key tables: `series_configuration`, `scrape_action_queue`, various rate/bond/economic indicator tables, `swedish_members_of_parliament` (JSONB), `swedish_member_activity`, `riksdag_party`/`riksdag_election`/`riksdag_election_result`/`riksdag_organ`, `local_election_area`/`local_election_party_result`/`local_governance`, `scb_indicator`/`scb_area_value`/`scb_availability`/`scb_table_state`.

Chart series in `series_configuration` carry picker metadata (V27), returned by `/series-config`: `category` (`Räntor`, `Statsobligationer`, `Statsskuld`, `Valuta`, `Nyckeltal USA`), `subcategory` (e.g. `Styrränta`, `Euromarknadsränta`, `Statsskuldväxel`, `Nominell`), `unit` (short Swedish display form taken from the source: `%`, `% av BNP`, `mn EUR`, `mn USD`, `mdr USD`, `SEK/USD`, `punkter`, `index`), `country` (Swedish name, null when not country-specific) and `maturity_months` (rates/bonds/bills only). A new chart series must set them, and `scrape_flow = 'MACRO_POINT'`, in its migration; the chart series are exactly the `MACRO_POINT` configs. `ChartSeriesConfigIntegrationTest` checks that every chart series has a unit and subcategory, lists the exact category set and row count, and checks the flow of every non-`MACRO_POINT` config.

## Testing

- JUnit 5 + Mockito for unit tests
- TestContainers (PostgreSQL) for integration tests — Docker must be running for `mvn test`
- `ArchitectureTest` (ArchUnit) checks the module boundaries: every class outside the root package lives in a module, `shared` or `jooq.generated` (so no layer-first package can come back), `shared` doesn't depend on any module, and a module's `domain`/`application` don't depend on its `adaptors`. A domain module (`DOMAIN_MODULES`) depends only on itself, `scrape`, `shared` and `jooq.generated`, and nothing outside it depends on it, apart from what `DOMAIN_MODULE_EXTRA_DEPENDENCIES` allows per module (another module's `application/port/in` and `domain` only; today `election` → `parliament`). Add a new module to `MODULES` (and to `DOMAIN_MODULES` for a business domain)
- Test fixtures in `src/test/resources/json/`
- `@ActiveProfiles("test")` activates `application-test.properties`

## Workflow

Agreed changes are implemented with the `/ship` skill (`.claude/skills/ship/SKILL.md`): an implementer sub-agent opens a PR, a separate reviewer sub-agent (Fable, or Opus when Fable credits run out) reviews it, then it is merged once the reviewer says LGTM. `main` is protected: every change needs a PR, and the `frontend` and `backend` jobs in `.github/workflows/ci.yml` (frontend: tracked-`.env` check, `npm run typecheck`, build, lint; backend: `mvn test`) must pass. No approval is required.

PRs also get an automatic Claude review comment from `.github/workflows/claude-review.yml` (comment-only, not a required check; a PR that changes that file is skipped, since the action requires it to match `main`). It needs the Claude GitHub App installed on the repo and a `CLAUDE_CODE_OAUTH_TOKEN` repo secret generated with `claude setup-token` (Pro/Max subscription).

## Commit Convention

Follows conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `config:`, `chore:`, `docs:`
