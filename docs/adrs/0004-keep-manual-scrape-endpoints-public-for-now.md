# 0004. Keep manual scrape endpoints public for now

## Status

Accepted. This is an accepted risk to revisit, not a permanent decision.

## Date

2026-11-01

## Context

`InsertionAdaptor` offers `POST /macro-analyzer/enqueue?name=`, `/enqueue-all` and `/scrape-from-queue`. The nginx template proxies everything under `/macro-analyzer/`, so the gateway exposes them. The backend has no authentication (no Spring Security in `pom.xml`), and `CorsConfiguration` allows `POST` from every origin.

The split is a natural moment to change this. The draft design recommended keeping these endpoints off the gateway. The project owner chose to keep them reachable.

## Decision

The manual scrape endpoints, or their per-service replacements, stay reachable through the gateway without authentication. The split doesn't change this. The exact per-service paths are an open question in the [migration guide](../microservice-migration-guide.md).

## Alternatives considered

- **Keep them off the gateway**, and reach each service directly when needed. This closes the exposure, but makes a manual scrape more awkward.
- **Add authentication.** This allows access from anywhere and closes the exposure, but adds Spring Security or an API key to a backend that has neither.

## Consequences

- Anyone who can reach the gateway can trigger scrapes, and so cause calls to the external APIs, database writes and, for SCB, a large heap spike.
- Each service's `scrapeLock` limits this to one run per service: a second `/scrape-from-queue` answers 409. Since each service has its own lock, four runs can go at once after the split.
- Nothing is deployed today, so the exposure is limited to whoever can reach a development machine. It grows with the first real deployment, which makes that deployment ([step 6 of the guide](../microservice-migration-guide.md#step-6-first-real-kubernetes-deployment)) the natural point to revisit this.
