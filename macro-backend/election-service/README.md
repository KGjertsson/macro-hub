# election-service

Valmyndigheten's final results: kommun (KF), region (RF) and Riksdag valkrets (RD) elections. It serves `/macro-analyzer/municipalities/*`, `/macro-analyzer/regions/*`, `/macro-analyzer/constituencies/*` and the manual scrape paths under `/macro-analyzer/scrape/election/` on port 8085, with its own database and role, `election`. It builds on the `macro-platform` library, for the scrape core and the shared code. The gateway (nginx in Compose, the proxy in `vite.config.ts`) routes these paths here.

The module's layout and behaviour are described in the repository's `CLAUDE.md`, and how it got its own database in [the migration guide](../../docs/microservice-migration-guide.md).

## Run it

All Maven commands run from `macro-backend/`. It needs no keys and no `.env`. Without settings it uses the Compose Postgres, `localhost:5432/election`. Any other database needs `SPRING_DATASOURCE_URL`.

```bash
JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl election-service -am spring-boot:run
```

In Compose it's the service `election`, from the image `election-service`:

```bash
sh ./mvnw clean package && docker build -t election-service election-service
```

An existing Postgres volume needs the database first. See "Starting the local stack" in `CLAUDE.md`.

## Database

On start-up, Flyway runs twice against the service's own database:

1. `macro-platform`'s `db/platform` creates `series_configuration` and `scrape_action_queue`, with the history table `flyway_platform_schema_history`.
2. This module's `db/migration`, with the default `flyway_schema_history`. `V1` is a squashed baseline of the three tables and the three config rows, which keep their `id` and `global_id`. `local_governance` starts empty: who governs has no open data source, so rows come by migration. The schema isn't empty when this run starts, so it uses a baseline at version 0.

To regenerate the jOOQ model (`election.jooq.generated`), from a database at the latest migration:

```bash
mvn -P jooq-codegen-election compile -pl election-service -am
```

The default is `localhost:5432/election`. For another database, add `-Djooq.codegen.url=...`. `macro-platform`'s model is generated from the same default database: see "Backend" in `CLAUDE.md`.

## Party colours

Election reads the Riksdag parties' colours from `parliament-service` (`election.parliament.url`, default `http://localhost:8084`, Compose sets `http://parliament:8084`), cached in memory. It starts without that service, uses Valmyndigheten's colours and keeps trying every 15 seconds until the list has loaded. `macro-backend/contracts/parliament-parties.json` pins the fields it reads, and `ParliamentPartiesClientTest` holds this service to that file. See [ADR 0005](../../docs/adrs/0005-cached-rest-between-services-and-no-message-broker.md).

## Fill it

```bash
curl -X POST http://localhost:8081/macro-analyzer/scrape/election/enqueue-all
curl -X POST http://localhost:8081/macro-analyzer/scrape/election/scrape-from-queue
```

`scrape-from-queue` returns when the run is done (about 2 minutes on an empty database), or answers 409 if one is already running. Later runs skip files whose md5 is unchanged, kept in `local_election_area.source_md5`. The service's cron drains the queue every minute, and enqueues everything during the 22:00 hour (UTC in the container).
