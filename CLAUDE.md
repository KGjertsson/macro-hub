# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

macro-hub visualizes macro-economic trends from multiple data sources (Riksbanken, Eurostat, Federal Reserve, Swedish Parliament). It consists of:

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
- shadcn-ui components live in `src/components/ui/` (configured via `components.json`)
- Backend base URL is hardcoded as `rootUrl = 'http://localhost:8080'` in `src/models/Constants.ts`; it calls `/macro-analyzer/series-config` and `/macro-analyzer/chart-data`
- **Port clash:** `vite.config.ts` also sets the dev server to port 8080. Start the backend first so Vite falls back to the next free port (8081); if Vite starts first, Spring Boot fails to bind. Backend CORS allows all origins.
- Package manager is npm; `package-lock.json` is the only lockfile (don't add `bun.lockb`/`yarn.lock`)

### Docker (from repo root)

```bash
# Dependencies only (PostgreSQL on localhost:31274)
docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up

# Full stack (PostgreSQL + backend on :8080) — build the image first:
#   (cd macro-backend/macro-analyzer && ./mvnw clean package && docker build -t macro-analyzer .)
docker compose -f macro-backend/macro-infra/docker/docker-compose-all.yml up
```

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

### Riksdagen (parliament) data

- `MembersOfParliament` scrape upserts every person from `data.riksdagen.se/personlista` (JSONB, keyed on `intressent_id`); `MembersOfParliamentActivity` then fetches voting totals, speeches and motions per member of the latest two terms (~600 members, ~7 min, throttled)
- Election seat counts are curated in `riksdag_election_result` (Flyway seed from Valmyndigheten) because Riksdagen only stores each person's *current* party. Add a new election as a migration (rows in `riksdag_election` + `riksdag_election_result`)
- Riksdagen configs (category `Riksdagen`) are excluded from `/series-config` so they don't appear in the chart picker

### Database

PostgreSQL 16 with Flyway migrations in `src/main/resources/db/migration/` (V1–V22). JOOQ generates type-safe table access. Key tables: `series_config`, `scrape_action_queue`, various rate/bond/economic indicator tables, `swedish_members_of_parliament` (JSONB), `swedish_member_activity`, `riksdag_party`/`riksdag_election`/`riksdag_election_result`/`riksdag_organ`.

## Testing

- JUnit 5 + Mockito for unit tests
- TestContainers (PostgreSQL) for integration tests — Docker must be running for `mvn test`
- Test fixtures in `src/test/resources/json/`
- `@ActiveProfiles("test")` activates `application-test.properties`

## Commit Convention

Follows conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `config:`, `chore:`, `docs:`
