# regional-statistics-service

SCB DeSO and RegSO statistics, for the Områdesstatistik page. It serves `/macro-analyzer/regional-statistics/*` and the manual scrape paths under `/macro-analyzer/scrape/regional-statistics/` on port 8082, with its own database and role, `regional_statistics`. It builds on the `macro-platform` library, for the scrape core and the shared code. The gateway (nginx in Compose, the proxy in `vite.config.ts`) routes both prefixes here.

It is the template for the other services in [the migration guide](../../docs/microservice-migration-guide.md). The module's layout and behaviour are described in the repository's `CLAUDE.md`.

## Run it

All Maven commands run from `macro-backend/`. Without settings it uses the Compose Postgres, `localhost:5432/regional_statistics`. Any other database needs `SPRING_DATASOURCE_URL`, and on a machine with microk8s, port 31274 can be the cluster's real database.

```bash
JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl regional-statistics-service -am spring-boot:run
```

In Compose it's the service `regional-statistics`, from the image `regional-statistics-service`:

```bash
sh ./mvnw clean package && docker build -t regional-statistics-service regional-statistics-service
```

An existing Postgres volume needs the database first. See "Starting the local stack" in `CLAUDE.md`.

## Database

On start-up, Flyway runs twice against the service's own database:

1. `macro-platform`'s `db/platform` creates `series_configuration` and `scrape_action_queue`, with the history table `flyway_platform_schema_history` (`macro.platform.flyway.enabled=true`).
2. This module's `db/migration`, with the default `flyway_schema_history`. `V1` is a squashed baseline of the tables, the indicators and the config row. The schema isn't empty when this run starts, so it uses a baseline at version 0.

To regenerate the jOOQ model (`regionalstatistics.jooq.generated`), from a database at the latest migration:

```bash
mvn -P jooq-codegen-regional-statistics compile -pl regional-statistics-service -am
```

The default is `localhost:5432/regional_statistics`. For another database, add `-Djooq.codegen.url=...`.

## Fill it

```bash
curl -X POST http://localhost:8081/macro-analyzer/scrape/regional-statistics/enqueue-all
curl -X POST http://localhost:8081/macro-analyzer/scrape/regional-statistics/scrape-from-queue
```

`scrape-from-queue` returns when the run is done, or answers 409 if one is already running. The service's cron drains the queue every minute, and enqueues everything during the 22:00 hour (UTC in the container).
