# macro-series-service

The chart series, from Riksbanken, the Fed and Eurostat. It serves `/macro-analyzer/series-config`, `/macro-analyzer/chart-data` and the manual scrape paths under `/macro-analyzer/scrape/macro-series/` on port 8083, with its own database and role, `macro_series`. It builds on the `macro-platform` library, for the scrape core and the shared code. The gateway (nginx in Compose, the proxy in `vite.config.ts`) routes these paths here.

The module's layout and behaviour are described in the repository's `CLAUDE.md`, and the extraction in [the migration guide's last version](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md).

## Keys

Riksbanken and Fed scrapes need `RIKSBANKEN_PROD_SUBSCRIPTION_KEY` and `FED_PROD_SUBSCRIPTION_KEY`. Copy `.env.example` to `.env` in this directory (it is gitignored, and must never be committed) and fill them in. Under `mvnw`, Spring reads the file from the working directory, which is this directory. Compose reads it with `--env-file macro-backend/macro-series-service/.env`. Without keys the service still starts and serves what it has stored.

## Run it

All Maven commands run from `macro-backend/`. Without settings it uses the Compose Postgres, `localhost:5432/macro_series`. Any other database needs `SPRING_DATASOURCE_URL`.

```bash
JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl macro-series-service -am spring-boot:run
```

In Compose it's the service `macro-series`, from the image `macro-series-service`:

```bash
sh ./mvnw clean package && docker build -t macro-series-service macro-series-service
```

An existing Postgres volume needs the database first. See "Starting the local stack" in `CLAUDE.md`.

## Database

On start-up, Flyway runs twice against the service's own database:

1. `macro-platform`'s `db/platform` creates `series_configuration` and `scrape_action_queue`, with the history table `flyway_platform_schema_history`.
2. This module's `db/migration`, with the default `flyway_schema_history`. `V1` is a squashed baseline of the 41 tables and the 108 config rows. The schema isn't empty when this run starts, so it uses a baseline at version 0. `V2` removes duplicates, keeping the newest row, and adds a unique constraint on series and date to all 41 tables, so scrapes can upsert.

The config rows keep their `id`s from before the split. `/series-config` lists them in that order, and `/chart-data` picks each series' colour from it.

To regenerate the jOOQ model (`macroseries.jooq.generated`), from a database at the latest migration:

```bash
mvn -P jooq-codegen-macro-series compile -pl macro-series-service -am
```

The default is `localhost:5432/macro_series`. For another database, add `-Djooq.codegen.url=...`.

## Fill it

```bash
curl -X POST http://localhost:8081/macro-analyzer/scrape/macro-series/enqueue-all
curl -X POST http://localhost:8081/macro-analyzer/scrape/macro-series/scrape-from-queue
```

`scrape-from-queue` returns when the run is done, or answers 409 if one is already running. The service's cron drains the queue every minute, and enqueues everything during the 22:00 hour (UTC in the container).

**Keep a local database that has data rather than re-creating it:** FRED serves the Dow Jones and S&P 500 for a rolling 10 years, so a re-scrape loses older days the database still has.
