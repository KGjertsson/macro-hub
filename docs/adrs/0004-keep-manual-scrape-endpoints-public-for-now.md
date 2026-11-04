# 0004. Keep manual scrape endpoints public for now

## Status

Accepted. This is an accepted risk to revisit, not a permanent decision.

## Date

2026-11-01

## Context

`InsertionAdaptor` offers `POST /macro-analyzer/enqueue?name=`, `/enqueue-all` and `/scrape-from-queue`. The nginx template proxies everything under `/macro-analyzer/`, so the gateway exposes them. The backend has no authentication (no Spring Security in `pom.xml`), and `CorsConfiguration` allows `POST` from every origin.

The split is a natural moment to change this. The draft design recommended keeping these endpoints off the gateway. The project owner chose to keep them reachable.

## Decision

The manual scrape endpoints, or their per-service replacements, stay reachable through the gateway without authentication. The split doesn't change this. The exact per-service paths are an open question in the [migration guide](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md).

*Note, 2026-11-01:* the paths are now decided, under `/macro-analyzer/scrape/<service>/`. See "Manual scrape endpoints" in the guide.

*Note, 2026-11-02:* the old `/macro-analyzer/enqueue`, `/enqueue-all` and `/scrape-from-queue` are gone with the monolith, and the gateway's `/macro-analyzer/` catch-all answers 404. The four per-service paths are still public, so this decision stands.

*Note, 2026-11-02, revisited at the first deployment (step 6):* the decision stands for now. The gateway's NodePort, 30081, answers on every address of the WSL2 machine, so whatever can reach that network can read the app and trigger the four manual scrape paths, with no authentication. **Still open, as the project owner's question:** how far that network reaches, and whether the exposure is acceptable or should be narrowed. The answer also bears on `POST /macro-analyzer/chart-data`, which isn't a scrape path: it needs no authentication either, and a chart of all 108 series takes 681 MiB of heap after a collection. Two at once would probably exceed macro-series' 1075 MiB cap and restart the service (inferred from that measurement, not tried).

*Note, 2026-11-04:* the NodePort exposure question above no longer applies, because [0007](0007-remove-the-kubernetes-deployment-until-its-needed.md) removed the deployment. The decision itself stands while the app only runs under Compose. **Whenever the app is exposed again, the manual scrape endpoints must be revisited,** and with them the cost of an unauthenticated `POST /macro-analyzer/chart-data`.

## Alternatives considered

- **Keep them off the gateway**, and reach each service directly when needed. This closes the exposure, but makes a manual scrape more awkward.
- **Add authentication.** This allows access from anywhere and closes the exposure, but adds Spring Security or an API key to a backend that has neither.

## Consequences

- Anyone who can reach the gateway can trigger scrapes, and so cause calls to the external APIs, database writes and, for SCB, a large heap spike.
- Each service's `scrapeLock` limits this to one run per service: a second `/scrape-from-queue` answers 409. Since each service has its own lock, four runs can go at once after the split.
- Nothing is deployed today, so the exposure is limited to whoever can reach a development machine. It grows with the first real deployment, which makes that deployment ([step 6 of the guide](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md#step-6-first-real-kubernetes-deployment)) the natural point to revisit this.
