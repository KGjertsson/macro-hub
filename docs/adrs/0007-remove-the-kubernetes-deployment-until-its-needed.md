# 0007. Remove the Kubernetes deployment until it's needed

## Status

Accepted. It amends [0006](0006-kubernetes-as-eventual-deployment-target.md) without superseding it: Kubernetes may still be the eventual target.

## Date

2026-11-04

## Context

Step 6 of the [migration guide](../microservice-migration-guide.md#step-6-first-real-kubernetes-deployment) deployed the app to microk8s under WSL2 on 2026-11-02, from `macro-backend/macro-infra/k8s/`. It was a test cluster: the scrapes and the requests through its gateway were tested, and no page was ever opened in a browser.

The project owner won't run Kubernetes on this machine, so nothing runs those files. CI doesn't use them, and neither does Compose, so a change to a service, its port or its migrations wouldn't show up as a broken manifest. They would fall behind unnoticed, as [0006](0006-kubernetes-as-eventual-deployment-target.md) already expected between deployments.

The project owner decided this.

## Decision

Delete `macro-backend/macro-infra/k8s/`, and create a deployment fresh when one is needed. Docker Compose is the only way to run the app until then.

Commit `584119f` (PR #91) has the last working version, for reference. Step 6 of the guide stays as the record, with its memory and GC measurements.

## Alternatives considered

- **Keep the files, unused.** Nothing would notice them falling behind, and `CLAUDE.md` would go on describing a deployment that doesn't run.
- **Keep them on a branch or in another folder, marked as unmaintained.** Commit `584119f` already gives this, with nothing to look after.

## Consequences

- This changes files only. It doesn't stop anything on a cluster that is running. The teardown script is in that commit: `git show 584119f:macro-backend/macro-infra/k8s/teardown.sh`.
- The cluster's nightly `pg_dump` goes with the files, and Compose's databases have none, so nothing here makes backups any more. `parliament` under Compose is still the only copy of its data, as `CLAUDE.md` says.
- Nothing limits the services' memory, as Compose has always run them: no container limit, no `-Xmx`, and the JVM's own choice of collector.
- Application code is unchanged. `spring-boot-starter-actuator` stays in each service, though only the deployment's probes used its health endpoints.
- The manual scrape endpoints have to be looked at again when the app is next exposed ([0004](0004-keep-manual-scrape-endpoints-public-for-now.md)).
- A new deployment starts from step 6's findings. It should run `microk8s enable rbac`, as this cluster was `AlwaysAllow`, and plan how a browser reaches it: under WSL NAT networking, a NodePort isn't reachable through `localhost` from Windows.
