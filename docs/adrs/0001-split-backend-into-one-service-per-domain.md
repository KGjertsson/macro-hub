# 0001. Split the backend into one service per domain

## Status

Accepted

## Date

2026-11-01

## Context

The backend is one Spring Boot application with the modules `scrape`, `macroseries`, `regionalstatistics`, `parliament`, `election` and `shared`. `ArchitectureTest` already enforces the module boundaries, and the only allowed dependency between domain modules is `election` on `parliament` (`PartyDirectory` and `Party`).

The domains behave differently at runtime. `regionalstatistics` holds about 80% of the data (`scb_area_value` is 370 MB of the 464 MB local database), and a first SCB scrape takes about 12 minutes and a few hundred MB of heap. All domains share the single `scrapeLock` in `ScrapeService`, so a long SCB run delays every other scrape.

Java line counts at the time of the decision: `macroseries` 3,739, `parliament` 1,886, `regionalstatistics` 1,811, `election` 1,344, `scrape` 924, `shared` 340.

## Decision

Split the backend into four services, one per business domain: macro-series, regional-statistics, parliament and election. The repo stays a monorepo. URLs don't change, because every controller already maps below `macro-analyzer`.

The split happens in the steps listed in the [migration guide](../microservice-migration-guide.md).

*Note, 2026-11-02:* the split is done. The monolith is retired (step 5 of the [migration guide](../microservice-migration-guide.md)), and `election-service` holds what was left of it. The base package `com.kg.macroanalyzer` and the URL prefix `/macro-analyzer` stay, as this ADR says URLs don't change. Renaming the package was out of scope.

## Alternatives considered

- **Extract only regional-statistics and keep the other three together.** This captures most of the runtime benefit for one extra JVM. Rejected in favour of the full split.
- **Keep the monolith.** This has the lowest running cost, but scrapes keep blocking each other, SCB's memory use stays shared, and the boundaries stay enforced only by a test.

## Consequences

- Scrapes no longer block each other, SCB's memory spikes are isolated and each service deploys on its own.
- Boundaries are enforced by the build and by separate databases ([0003](0003-one-database-per-service-on-one-postgres-instance.md)).
- Memory use goes up. A `docker stats` reading of the monolith showed 609 MiB (the number moves with the JVM's heap; another reading showed 890 MiB), and four JVMs would plausibly take 1.5 to 2 GiB. That is an estimate, not a measurement, so don't size memory limits from it. *Note, 2026-11-02:* step 6 of the guide measured it on the cluster, with the table there: about 1.1 GiB idle just after a restart, and 3.1 GiB for the first scrape's peaks together, under Serial. The guide explains why those peaks overstate the need, and why the services now run G1.
- Local start-up becomes four services and a proxy, and there are four images to build and deliver.
- This is a lot of operational surface for about 10,000 lines of Java running as one replica. It is an accepted cost.
