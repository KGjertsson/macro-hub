# Architecture decision records

This folder records the larger decisions in macro-hub: choices that shape how the system is built and are expensive to reverse. Small choices belong in the PR that makes them. Each ADR says what was decided, why, what else was considered and what follows from it.

The split into services was tracked step by step in the [migration guide](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md), linked at its last version. [`docs/architecture.md`](../architecture.md) shows the result.

## Index

| Number | Title | Status | Date |
| --- | --- | --- | --- |
| [0001](0001-split-backend-into-one-service-per-domain.md) | Split the backend into one service per domain | Accepted | 2026-11-01 |
| [0002](0002-keep-scrape-as-a-shared-library.md) | Keep scrape as a shared library | Accepted | 2026-11-01 |
| [0003](0003-one-database-per-service-on-one-postgres-instance.md) | One database per service on one Postgres instance | Accepted | 2026-11-01 |
| [0004](0004-keep-manual-scrape-endpoints-public-for-now.md) | Keep manual scrape endpoints public for now | Accepted | 2026-11-01 |
| [0005](0005-cached-rest-between-services-and-no-message-broker.md) | Cached REST between services and no message broker | Accepted | 2026-11-01 |
| [0006](0006-kubernetes-as-eventual-deployment-target.md) | Kubernetes as the eventual deployment target | Accepted | 2026-11-01 |
| [0007](0007-remove-the-kubernetes-deployment-until-its-needed.md) | Remove the Kubernetes deployment until it's needed | Accepted | 2026-11-04 |

## Statuses

- **Proposed:** written down, but not yet decided.
- **Accepted:** decided. The code should follow it, or be moving towards it.
- **Superseded:** replaced by a later ADR, which the status line names.

## Adding an ADR

1. Copy the layout of an existing ADR to `NNNN-short-title.md`, using the next free number. The sections are Status, Date, Context, Decision, Alternatives considered and Consequences.
2. Keep it short, and use concrete evidence from the code rather than general arguments.
3. Add a row to the index above, in the same PR.
4. Record who decided when it isn't obvious, for example when a recommendation was not followed.

Don't rewrite an accepted ADR. If a decision changes, add a new ADR, set the old one's status to `Superseded by NNNN` and update the index. Fixing a typo or a broken link is fine.
