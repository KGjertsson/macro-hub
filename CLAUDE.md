# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

macro-hub visualizes macro-economic trends from multiple data sources (Riksbanken, Eurostat, Federal Reserve, Swedish Parliament, Valmyndigheten). It consists of:

- **macro-backend/macro-analyzer/** — Java 25 / Spring Boot 4.1.2 backend
- **macro-frontend/** — Vite + React 18 + TypeScript + shadcn-ui dashboard (formerly the separate `macro-view-hub` repo, merged in via `git subtree`)
- **macro-backend/macro-infra/** — Docker Compose files (`docker/`) and microk8s manifests (`k8s/`)

## Build & Test Commands

### Toolchain requirements

- **JDK 25** for the backend (installed via SDKMAN). If `java -version` isn't 25 (e.g. an IDE exported an older `JAVA_HOME`), set `JAVA_HOME=~/.sdkman/candidates/java/current` for Maven commands.
- Lombok is registered under `annotationProcessorPaths` in the compiler plugin because JDK 23+ no longer runs annotation processors found on the classpath; without it the build fails with `cannot find symbol` errors (`log`, getters/builders).
- **Node 20.19+ or 22.12+** for the frontend (Vite 7). Node 18 fails with `crypto.hash is not a function`.
- `mvnw` is not committed as executable; run it as `sh ./mvnw ...` (or `chmod +x mvnw`).
- Docker must be running for Postgres and for `mvn test` (TestContainers).

### Starting the local stack (in this order)

Both the backend and Vite want port 8080, and the frontend calls the backend at `http://localhost:8080`, so the order matters.

1. **Check what is already running:** `lsof -nP -iTCP -sTCP:LISTEN | grep -E ':(8080|8081|31274)'`. If an older backend holds 8080, it must be restarted to serve new code.
2. **Postgres** (from repo root): `docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up -d`. It listens on `localhost:31274`.
3. **Backend** (from `macro-backend/macro-analyzer/`, so `.env` is picked up): `SPRING_DATASOURCE_PASSWORD=macropassword JAVA_HOME=~/.sdkman/candidates/java/current sh ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`. Wait until it serves requests on 8080 (e.g. `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/macro-analyzer/series-config` returns 200) before starting Vite. Flyway migrations run on startup.
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
SPRING_DATASOURCE_PASSWORD=macropassword sh ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
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
- Kommun/region maps (`ChoroplethMap`) render `public/geo/kommuner.topo.json` and `lan.topo.json` with d3-geo. They are generated from SCB's CC0 boundary shapefiles by `scripts/build-geo.sh` (run from `macro-frontend/`; needs network and `npx`), and the outputs are committed
- shadcn-ui components live in `src/components/ui/` (configured via `components.json`)
- Backend base URL is hardcoded as `rootUrl = 'http://localhost:8080'` in `src/models/Constants.ts`; it calls `/macro-analyzer/series-config` and `/macro-analyzer/chart-data`
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

- **`adaptors/http/`** — REST controllers. `InsertionAdaptor` handles enqueue/scrape endpoints; `ExtractionAdaptor` handles chart data retrieval.
- **`adaptors/database/postgres/`** — `PostgresAdaptor` implements `DatabasePort` using JOOQ. Contains per-table repositories.
- **`adaptors/webadaptor/`** — Fetches external API data. `MacroPointAdaptorFlowFactory` routes to the correct connection builder and response parser per data source. `MembersOfParliamentWebAdaptor` handles parliament data.
- **`adaptors/cron/`** — Scheduled tasks: `CronScrapeAdaptor` drains the scrape queue every 60s; `CronEnqueueAdaptor` enqueues all series every minute during the 22:00 hour.
- **`application/ports/driving/`** — `InPort` (write operations), `OutPort` (read operations).
- **`application/ports/driven/`** — `DatabasePort`, `WebPort`.
- **`application/domain/`** — Core domain: `MacroSeries`, `MacroPoint`, `AlignedBundle`, parliament models. Uses Java records for value objects.
- **`application/services/`** — `ScrapeService` uses Strategy pattern with `ScrapeQueueKind` to route to the correct `ScrapeFlow`. Other services: `EnqueueService`, `TimeFilterService`, `AlignmentService`, `LabelGenerationService`.
- **`configuration/`** — Spring `@Configuration` beans for CORS, Jackson, schedulers, scrape flows, JDBC.
  - Jackson 3 (`tools.jackson`) is used throughout. `CorsConfiguration` has `@EnableWebMvc`, which disables Boot's MVC auto-config, so `JacksonConfiguration` registers its `JsonMapper` with MVC explicitly. It keeps `WRITE_DATES_AS_TIMESTAMPS` enabled because the frontend expects date labels as arrays (`[1994,6]`).
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
- `GET /macro-analyzer/municipalities/elections?year=` / `GET /macro-analyzer/regions/elections?year=` — per kommun/region: seats, turnout, votes and seats per party (default: latest year)
- `GET /macro-analyzer/municipalities/{kommunkod}` / `GET /macro-analyzer/regions/{länskod}` — the same plus elected members and curated governance

### Riksdagen (parliament) data

- `MembersOfParliament` scrape upserts every person from `data.riksdagen.se/personlista` (JSONB, keyed on `intressent_id`); `MembersOfParliamentActivity` then fetches voting totals, speeches and motions per member of the latest two terms (~600 members, ~7 min, throttled)
- Election seat counts are curated in `riksdag_election_result` (Flyway seed from Valmyndigheten) because Riksdagen only stores each person's *current* party. Add a new election as a migration (rows in `riksdag_election` + `riksdag_election_result`)
- Riksdagen configs (category `Riksdagen`) are excluded from `/series-config` so they don't appear in the chart picker

### Kommun and region elections (Valmyndigheten)

- `MunicipalElections` (KF) and `RegionElections` (RF) scrape the final results from `resultat.val.se`: `index.md5` lists one zip per kommun/region, and only the `mandatfordelning` JSON inside each is parsed (votes, seats, elected members). First run downloads ~310 zips (~1.5 min); later runs skip files whose md5 is unchanged, so corrected files are picked up automatically
- `scrape_url` (`.../resultatfiler/val2026`) selects the election; point it at the next election's folder with a migration. Gotland has no regionfullmäktige
- Who governs (`local_governance`) has no open data source and starts empty; add rows by migration (party abbreviations as in the results). The frontend only shows the "styre" section where a row exists
- Configs use category `Valmyndigheten` and are also kept out of `/series-config`

### Database

PostgreSQL 16 with Flyway migrations in `src/main/resources/db/migration/` (V1–V23). JOOQ generates type-safe table access. Key tables: `series_config`, `scrape_action_queue`, various rate/bond/economic indicator tables, `swedish_members_of_parliament` (JSONB), `swedish_member_activity`, `riksdag_party`/`riksdag_election`/`riksdag_election_result`/`riksdag_organ`, `local_election_area`/`local_election_party_result`/`local_governance`.

## Testing

- JUnit 5 + Mockito for unit tests
- TestContainers (PostgreSQL) for integration tests — Docker must be running for `mvn test`
- Test fixtures in `src/test/resources/json/`
- `@ActiveProfiles("test")` activates `application-test.properties`

## Commit Convention

Follows conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `config:`, `chore:`, `docs:`
