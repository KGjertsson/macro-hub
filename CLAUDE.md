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
mvn test -Dtest=ExtractionAdaptorTest

# Run a single test method
mvn test -Dtest=ExtractionAdaptorTest#shouldReturnOk_whenValidBundleRetrieved

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
  - `.githooks/pre-commit` rejects commits that stage `.env` files (even via `git add -f`). Enable it once per clone: `git config core.hooksPath .githooks`

### Frontend (from `macro-frontend/`)

```bash
npm install
npm run dev        # Vite dev server
npm run build      # production build
npm run build:dev  # development-mode build
npm run lint
npm run preview    # serve the production build
```

- Uses Recharts for charts, TanStack Query, React Router; `@/` aliases `src/`
- Kommun/region/valkrets maps (`ChoroplethMap`) render `public/geo/kommuner.topo.json`, `lan.topo.json` and `valkretsar.topo.json` with d3-geo. They are generated from SCB's CC0 boundary shapefiles by `scripts/build-geo.sh` (run from `macro-frontend/`; needs network and `npx`), and the outputs are committed. Valkretsar are dissolved from the kommun layer via `scripts/valkretsar.csv` (kommunkod → valkrets); update it if the valkrets division changes
- DeSO/RegSO boundaries for the Områdesstatistik map are in `public/geo/deso_2018.topo.json`, `deso_2025.topo.json`, `regso_2020.topo.json` and `regso_2025.topo.json` (object name = file stem; properties `code` (desokod/regsokod without version suffix), `name` (`regsonamn`, or `"<kommunnamn> <desokod>"` for DeSO), `kommunkod`, and on DeSO also `regsokod`). They are generated from SCB's WFS (`geodata.scb.se/geoserver/stat/wfs`, CC0) by `scripts/build-geo-scb-areas.sh` (run from `macro-frontend/`; needs network, `npx` and `node`, ~2 min). The script fails if a layer's feature count differs from the WFS hit count or a code is missing/duplicated. SCB's 2025 layers extend into territorial waters, so the script clips them to the dissolved land outline of DeSO 2018 / RegSO 2020 (lakes stay inside areas in every version). Simplification is Douglas-Peucker with a tolerance scaled by area (~4 m for inner-city areas, up to 150 m for rural ones) to keep the files at ~3.0 MB (DeSO) and ~1.9 MB (RegSO); `quantization=1e6` keeps vertices within ~1.5 m for street-level zoom
- The Områdesstatistik page (`/omraden`, `src/pages/RegionalStatistics.tsx`, components in `src/components/regionalmap/`) is lazy-loaded so MapLibre GL (`maplibre-gl`, pinned exact; its worker is set via `setWorkerUrl` from a `?url` import because bundling breaks the library's own lookup) stays out of the main bundle. The basemap is OpenFreeMap's `positron` style (no API key; attribution OpenFreeMap/OpenMapTiles/OpenStreetMap comes from the style, SCB is added). The values' `geoVersion` picks which boundary file is fetched (TanStack Query, cached per version). Areas are coloured in 7 quantile classes via feature-state (blue sequential; red–gray–blue diverging when values have both signs, e.g. flyttnetto). URL state: `l` (DeSO|RegSO), `i` (indicator id), `y` (year), `a` (area code); an indicator without DeSO data is shown at RegSO. Address search uses Photon (`photon.komoot.io`, bbox around Sweden, results filtered to `countrycode=SE`; Photon rejects `lang=sv`, so no lang is sent) and finds the containing area with d3-geo `geoContains`
- shadcn-ui components live in `src/components/ui/` (configured via `components.json`)
- Backend base URL is hardcoded as `rootUrl = 'http://localhost:8080'` in `src/models/Constants.ts`; it calls `/macro-analyzer/series-config`, `/macro-analyzer/chart-data` and the parliament, election and `regional-statistics` endpoints
- **Port clash:** `vite.config.ts` also sets the dev server to port 8080. Start the backend first so Vite falls back to the next free port (8081); if Vite starts first, Spring Boot fails to bind. Backend CORS allows all origins.
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

- The frontend container (`macro-frontend/Dockerfile`) builds with Node 22 and serves `dist/` with nginx; `nginx.conf` falls back to `index.html` for `BrowserRouter` routes. It replaces the Vite dev server on 8081, so stop Vite first

## Architecture

The backend uses **Hexagonal Architecture (Ports & Adapters)** with **Domain-Driven Design**:

```
HttpInsertionAdaptor ──→ InPort  ──→ domain ──→ DatabasePort ──→ PostgresAdaptor
HttpExtractionAdaptor ─→ OutPort ─→ domain ──→ WebPort ──────→ WebAdaptors
CronAdaptor ───────────→ InPort
```

### Package structure (`com.kg.macroanalyzer`)

The backend is moving from packages by layer to packages by domain. Modules (`scrape` and `regionalstatistics` so far) use `domain/`, `application/port/in/`, `application/port/out/`, `application/service/` and `adaptors/{http,postgres,web,config,cron}/`; code not yet moved still sits in the layer packages below.

- **`scrape/`** — the scrape core, independent of the data sources:
  - `domain/` — `ScrapeQueueItem`, `ScrapeQueueKind`, `SeriesConfig` (every scrape flow's config)
  - `application/port/in/` — `InPort` (enqueue, enqueue-all, scrape-from-queue); `application/port/out/` — `ScrapeQueuePort` (queue reads/writes, `markAsDone`) and `SeriesConfigPort`
  - `application/service/` — `ScrapeService`, `EnqueueService`, and in `scrapeflow/` the `ScrapeFlow` pipeline (`Preparer` → `ExistingFinder` → `Scraper` → `Persister`) and `KindedScrapeFlow`. Each domain contributes one `KindedScrapeFlow` bean per `ScrapeQueueKind` (from each module's `adaptors/config/`, or the `*ScrapeFlowConfiguration` classes in `configuration/scrapeflow/` for code not yet moved); `ScrapeService` routes queue items by kind and fails at startup if a kind has no flow or more than one
  - `adaptors/http/` — `InsertionAdaptor` (enqueue/scrape endpoints); `adaptors/cron/` — `CronScrapeAdaptor` drains the queue every 60s, `CronEnqueueAdaptor` enqueues all series every minute during the 22:00 hour; `adaptors/postgres/` — queue and series-config repositories; `adaptors/config/` — preparer, series-config list and scheduler beans
- **`regionalstatistics/`** — SCB DeSO/RegSO statistics, the first domain module:
  - `domain/` — `GeoVersion`, `ScbLevel`, `ScbIndicator`, `ScbValues` and the other SCB value types
  - `application/port/in/` — `RegionalStatisticsQueries` (indicators and values) and its DTOs; `application/port/out/` — `RegionalStatisticsRepositoryPort`, the scrape payload `ConfigWithScbIndicators` and the SCB web requests
  - `application/service/` — `RegionalStatisticsService`, and in `scrape/` the scrape flow (`ExistingScbTableFinder`, `ScbRegionalScraper`, `ScbRegionalPersister`)
  - `adaptors/http/` — `RegionalStatisticsController` (`/regional-statistics/*`); `adaptors/postgres/` — `RegionalStatisticsPostgresAdaptor` and `ScbRegionalRepository`; `adaptors/web/` — SCB web adaptors, rate limiter, retry, connection builder and JSON-stat parsers; `adaptors/config/` — the SCB web beans and the scrape flow
- **`shared/`** — used by every module, depends on none of them: `config/` (CORS, Jackson, JDBC, Flyway catalog version) and `web/` (`WebPort`, `ScrapeException`, `WebAdaptorFlow`, `ConnectionBuilder`/`Getter`/`ResponseParser`, `HttpGetter`, `ZipEntryGetter`, `HttpResponses`)
- **`adaptors/http/`** — `ExtractionAdaptor` handles series config, chart data and the parliament and election reads.
- **`adaptors/database/postgres/`** — `PostgresAdaptor` implements `DatabasePort` using JOOQ. Contains per-table repositories.
- **`adaptors/webadaptor/`** — Source-specific web adaptors, connection builders and response parsers. `MacroPointAdaptorFlowFactory` routes to the correct connection builder and response parser per data source. `MembersOfParliamentWebAdaptor` handles parliament data.
- **`application/ports/driving/`** — `OutPort` (read operations).
- **`application/ports/driven/`** — `DatabasePort` and the domain-specific `ConfigWith*` scrape payloads and web requests.
- **`application/domain/`** — Core domain: `MacroSeries`, `MacroPoint`, `AlignedBundle`, parliament models. Uses Java records for value objects.
- **`application/services/`** — Domain services (`TimeFilterService`, `AlignmentService`, `LabelGenerationService`, parliament and election) and, in `scrape/scrapeflow/`, the domain-specific finders, scrapers and persisters.
- **`configuration/`** — `WebAdaptorConfiguration` and the per-domain `scrapeflow/*ScrapeFlowConfiguration` beans.
  - Jackson 3 (`tools.jackson`) is used throughout. `CorsConfiguration` (in `shared/config/`) has `@EnableWebMvc`, which disables Boot's MVC auto-config, so `JacksonConfiguration` registers its `JsonMapper` with MVC explicitly. It keeps `WRITE_DATES_AS_TIMESTAMPS` enabled because the frontend expects date labels as arrays (`[1994,6]`).
- **`jooq/generated/`** — Auto-generated JOOQ classes (do not edit manually).

### REST API Endpoints

- `GET /macro-analyzer/series-config` — list all series configurations
- `POST /macro-analyzer/chart-data` — retrieve aligned chart data
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
- Riksdagen configs (category `Riksdagen`) are excluded from `/series-config` so they don't appear in the chart picker

### Kommun, region and Riksdag elections (Valmyndigheten)

- `MunicipalElections` (KF) and `RegionElections` (RF) scrape the final results from `resultat.val.se`: `index.md5` lists one zip per kommun/region, and only the `mandatfordelning` JSON inside each is parsed (votes, seats, elected members). First run downloads ~310 zips (~1.5 min); later runs skip files whose md5 is unchanged, so corrected files are picked up automatically
- `ParliamentElections` (RD) uses the same pipeline, but its single zip (`s/rd/..._00_RD.zip`) holds all 29 valkretsar under `valkretsLista`, so the parser returns one area per valkrets (area code `01`..`29`, Gotland is `09`). Valkrets seats include the utjämningsmandat assigned to that valkrets (they sum to 349). The ~200 MB per-district `rostfordelning` file in the zip is streamed past, not parsed
- `scrape_url` (`.../resultatfiler/val2026`) selects the election; point it at the next election's folder with a migration. Gotland has no regionfullmäktige
- Who governs (`local_governance`) has no open data source and starts empty; add rows by migration (party abbreviations as in the results). The frontend only shows the "styre" section where a row exists
- Configs use category `Valmyndigheten` and are also kept out of `/series-config`

### SCB regional statistics (DeSO / RegSO)

- `ScbRegionalStatistics` (category `SCB`, kept out of `/series-config`) scrapes curated indicators from SCB's PxWeb API v2 (`scrape_url` `https://statistikdatabasen.scb.se/api/v2`, CC0). Indicators live in `scb_indicator` (seeded by V29): `numerator`/`denominator` are JSONB lists of `{table, contents, select}`; every table dimension except `Region` and `Tid` must be selected, codes within a dimension are summed, a later source replaces an earlier one per year and version, and with a denominator the value is `sum(num) / sum(den) * multiplier`
- One SCB table holds every area version; the region code tells them apart: `DeSO2018` (`0114A0010`), `DeSO2025` (`0114A0010_DeSO2025`), `RegSO2020` (`0114R001`), `RegSO2025` (`0114R001_RegSO2025`). Data is requested per version with a PxWeb wildcard (`?????????`, `*_DeSO2025`, …) and chunked by year to stay below 150 000 cells per call. Years a version has no data for are 0/null placeholders in SCB; a (version, year) is only kept when a value is non-null and non-zero. `area_code` is stored without the suffix so it matches the boundary files
- `scb_table_state` keeps SCB's `updated` stamp per table; an indicator is skipped when all its tables are unchanged and it already has rows. Stamps are written once every indicator of the run is stored; a table whose fetch failed, and every table of an indicator whose write failed, loses its stamp so the indicators using it are fetched again. To change an existing indicator's selection, also delete its `scb_area_value` and `scb_availability` rows in the migration so it is re-fetched (whether an indicator has data is read from `scb_availability`, which the persister keeps in step with the values)
- Calls are spaced by `scb.delay-ms` (default 350 ms, SCB allows 30 calls per 10 s) and retried up to 8 times: some SCB servers intermittently answer "Non-existent table" (404/400) for tables that exist. A first full scrape takes about 12 minutes (2.4 M rows, ~370 MB) because SCB spends several seconds on each uncached DeSO request; a run with unchanged tables takes about 30 seconds
- A full scrape keeps every fetched value in memory until the persister runs (~2.4 M values plus the per-source maps, a few hundred MB of heap). The JVM has no `-Xmx` and the pods no memory limit today; if either is added, leave room for this or the first full scrape can run out of memory

### Database

PostgreSQL 16 with Flyway migrations in `src/main/resources/db/migration/` (V1–V30). JOOQ generates type-safe table access. Key tables: `series_configuration`, `scrape_action_queue`, various rate/bond/economic indicator tables, `swedish_members_of_parliament` (JSONB), `swedish_member_activity`, `riksdag_party`/`riksdag_election`/`riksdag_election_result`/`riksdag_organ`, `local_election_area`/`local_election_party_result`/`local_governance`, `scb_indicator`/`scb_area_value`/`scb_availability`/`scb_table_state`.

Chart series in `series_configuration` carry picker metadata (V27), returned by `/series-config`: `category` (`Räntor`, `Statsobligationer`, `Statsskuld`, `Valuta`, `Nyckeltal USA`), `subcategory` (e.g. `Styrränta`, `Euromarknadsränta`, `Statsskuldväxel`, `Nominell`), `unit` (short Swedish display form taken from the source: `%`, `% av BNP`, `mn EUR`, `mn USD`, `mdr USD`, `SEK/USD`, `punkter`, `index`), `country` (Swedish name, null when not country-specific) and `maturity_months` (rates/bonds/bills only). A new chart series must set them in its migration; `ChartSeriesConfigIntegrationTest` checks that every chart series has a unit and subcategory and lists the exact category set and row count.

## Testing

- JUnit 5 + Mockito for unit tests
- TestContainers (PostgreSQL) for integration tests — Docker must be running for `mvn test`
- `ArchitectureTest` (ArchUnit) checks the module boundaries: modules and `shared` don't depend on the legacy `adaptors`/`application`/`configuration` packages, `shared` doesn't depend on any module, and a module's `domain`/`application` don't depend on its `adaptors`. A domain module (`DOMAIN_MODULES`) depends only on itself, `scrape`, `shared` and `jooq.generated`, and nothing outside it depends on it. Add a module to `MODULES` (and to `DOMAIN_MODULES` for a business domain) once it has moved out of the layer packages
- Test fixtures in `src/test/resources/json/`
- `@ActiveProfiles("test")` activates `application-test.properties`

## Workflow

Agreed changes are implemented with the `/ship` skill (`.claude/skills/ship/SKILL.md`): an implementer sub-agent opens a PR, a separate reviewer sub-agent (Fable, or Opus when Fable credits run out) reviews it, then it is merged once the reviewer says LGTM. `main` is protected: every change needs a PR, and the `frontend` and `backend` jobs in `.github/workflows/ci.yml` (build, lint, `mvn test`) must pass. No approval is required.

PRs also get an automatic Claude review comment from `.github/workflows/claude-review.yml` (comment-only, not a required check; a PR that changes that file is skipped, since the action requires it to match `main`). It needs the Claude GitHub App installed on the repo and a `CLAUDE_CODE_OAUTH_TOKEN` repo secret generated with `claude setup-token` (Pro/Max subscription).

## Commit Convention

Follows conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `config:`, `chore:`, `docs:`
