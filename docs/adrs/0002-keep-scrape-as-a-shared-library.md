# 0002. Keep scrape as a shared library

## Status

Accepted

## Date

2026-11-01

## Context

`scrape` holds no business data, only the queue, the configs and the generic pipeline. Every domain plugs into it in-process: each module registers a `RegisteredScrapeFlow`, `ScrapeService` routes a queue item to its flow by the config's `scrape_flow`, and each persister writes its data and calls `ScrapeQueuePort.markAsDone`. `shared` is already free of module dependencies.

Nothing in the frontend calls the enqueue or scrape endpoints.

## Decision

`scrape` and `shared` become a library, `macro-platform`, built in a Maven reactor in this repo. Each service runs its own cron jobs, queue and flows, using `series_configuration` and `scrape_action_queue` tables in its own database.

## Alternatives considered

- **Central scrape service.** Rejected. It would call the domain services over HTTP for runs of up to 12 minutes, and the queue update and the data write could never share a transaction.

## Consequences

- There are no calls between services for scraping, and each service has its own lock, so a 12-minute SCB run stops delaying the others.
- The queue and the data sit in one database, so they can share a transaction. They don't today: the persisters call `markAsDone` after the repositories' own jOOQ transactions have committed.
- The global `enqueue-all` goes away. A small script that calls each service replaces it.
- All four services would enqueue during the 22:00 hour (`CronEnqueueAdaptor`) and write concurrently. Stagger the hours if that strains Postgres.
- A change to `macro-platform` rebuilds every service.
- `macro-platform` ships the DDL for the two tables, so its migrations must not collide with those of the services. The [migration guide](../microservice-migration-guide.md) lists how as an open question.
- A central view of all scrapes would need new work. Wanting one is the main reason to reconsider this decision.
