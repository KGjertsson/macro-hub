# parliament-service

Riksdagen: members, their activity, parties and elections. It serves `/macro-analyzer/parliament/*` and the manual scrape paths under `/macro-analyzer/scrape/parliament/` on port 8084, with its own database and role, `parliament`. It builds on the `macro-platform` library, for the scrape core and the shared code. The gateway (nginx in Compose, the proxy in `vite.config.ts`) routes these paths here.

`GET /macro-analyzer/parliament/parties` is also an API between services: election reads the party colours from it. `macro-backend/contracts/parliament-parties.json` pins the fields it reads, and `PartiesContractHttpTest` holds this service to that file.

The module's layout and behaviour are described in the repository's `CLAUDE.md`, and the extraction in [the migration guide](../../docs/microservice-migration-guide.md).

## Run it

All Maven commands run from `macro-backend/`. It needs no keys and no `.env`. Without settings it uses the Compose Postgres, `localhost:5432/parliament`. Any other database needs `SPRING_DATASOURCE_URL`, and on a machine with microk8s, port 31274 is the cluster's real database.

```bash
JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl parliament-service -am spring-boot:run
```

In Compose it's the service `parliament`, from the image `parliament-service`:

```bash
sh ./mvnw clean package && docker build -t parliament-service parliament-service
```

An existing Postgres volume needs the database first. See "Starting the local stack" in `CLAUDE.md`.

## Database

On start-up, Flyway runs twice against the service's own database:

1. `macro-platform`'s `db/platform` creates `series_configuration` and `scrape_action_queue`, with the history table `flyway_platform_schema_history` (`macro.platform.flyway.enabled=true`).
2. This module's `db/migration`, with the default `flyway_schema_history`. `V1` is a squashed baseline of the six tables, the curated rows of `riksdag_party`, `riksdag_election`, `riksdag_election_result` and `riksdag_organ`, and the two config rows, which keep the monolith's `id` and `global_id`. The schema isn't empty when this run starts, so it uses a baseline at version 0.

To regenerate the jOOQ model (`parliament.jooq.generated`), from a database at the latest migration:

```bash
mvn -P jooq-codegen-parliament compile -pl parliament-service -am
```

The default is `localhost:5432/parliament`. For another database, add `-Djooq.codegen.url=...`.

## Fill it

**Keep a local database that has data rather than re-creating it**: a scrape only fetches activity for the latest two terms, and the monolith's V38 dropped the copy it was restored from. See "Restoring parliament" in the guide. An empty one fills like this:

```bash
curl -X POST http://localhost:8081/macro-analyzer/scrape/parliament/enqueue-all
curl -X POST http://localhost:8081/macro-analyzer/scrape/parliament/scrape-from-queue
```

`scrape-from-queue` returns when the run is done, or answers 409 if one is already running. The service's cron drains the queue every minute, and enqueues everything during the 22:00 hour (UTC in the container).
