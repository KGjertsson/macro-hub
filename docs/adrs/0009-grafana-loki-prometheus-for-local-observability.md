# 0009. Grafana, Loki and Prometheus for local observability

## Status

Accepted.

## Date

2026-11-08

## Context

The app runs on Docker Compose on one machine, as six containers ([0007](0007-remove-the-kubernetes-deployment-until-its-needed.md)). Its logs are only reachable with `docker logs`, one container at a time, and go when a container is re-created. Nothing records a container's CPU, memory, network or restarts over time. A failed scrape or a memory peak during a first SCB scrape can only be looked at while it happens.

## Decision

Add a local observability stack in `macro-backend/macro-infra/docker/docker-compose-observability.yml`, without changing the application:

- **Grafana** as the single UI, with its data sources provisioned from a file.
- **Loki** for logs, on a local filesystem, with 30 days' retention.
- **Grafana Alloy** to collect the logs through the Docker socket, only from the containers of the Compose project `docker`, because other projects run on the same machine. The labels are `service` and `container`, which stay few: one per container of the app.
- **Prometheus** for metrics, with 30 days' or 5 GB's retention, which scrapes **cAdvisor** for per-container CPU, memory, network and start times.

It is a Compose project of its own, `macrohub-observability`, with its own named volumes, and never references `docker_pgdata`. It joins the app's `docker_default` network as an external network, so that Prometheus can later scrape the services by their Compose names. Every image is pinned to an exact version. Only Grafana and Prometheus are published, on 127.0.0.1.

## Alternatives considered

- **Elasticsearch and Kibana.** Full-text indexing is more than this needs, and it costs a JVM with a heap of its own and a second UI, because metrics would still need Grafana. Loki only indexes the labels and scans the lines of the matching streams, which is enough search for the logs of six containers, and logs and metrics are side by side in one UI.
- **Promtail.** It has reached end of life, and Grafana names Alloy as its replacement.
- **Docker's Loki logging driver.** It needs a plugin installed in the daemon and a change to every service in `docker-compose-all.yml`, and a Loki that is down can block the containers logging to it. Reading through the socket needs no change to the app.
- **One Compose file with the app.** A restart of the observability stack would then be a change to the project that holds `docker_pgdata`, and a separate file keeps the stack optional.

## Consequences

- `docker_default` has to exist before this project starts, so the app stack must have been created once. While the observability stack is attached, a `docker compose down` of the app stack can't remove that network, which it leaves in place.
- The Docker root is `DOCKER_ROOT`, by default `/var/lib/docker`, because a snap install has it elsewhere. On snap under WSL2, cAdvisor reports per-container CPU, memory and network with the container's name.
- cAdvisor is privileged and Alloy can read the Docker socket, including every container's environment through its API, which holds the database passwords. Neither publishes a port, and this is a local stack; it would need another look before running anywhere shared.
- cAdvisor reports every container on the host, not only the app's. Its labels are limited to the name, image and the two Compose labels.
- Nothing in CI starts this stack, so a version bump or a config change is only tested by starting it.
- This is the container level only: no application metrics, dashboards or alerts, and logs are plain text lines.
  - *Note, 2026-11-08:* the services now log JSON under Compose and expose Prometheus metrics on their own ports, and the gateway logs its access lines as JSON, so `level` and, for the gateway, `route` are Loki labels besides `service` and `container`. Dashboards are provisioned from files. Alerts are still left out.
  - *Note, 2026-11-09:* scrape metrics, a Scrapes dashboard and Grafana-managed alert rules, provisioned from files, are added. There is no contact point, so alerts are only visible in Grafana.
  - *Note, 2026-11-09:* postgres_exporter reports the one Postgres instance's statistics, with a Postgres dashboard and two alert rules. It logs in as a `pg_monitor` role that may only connect to the `postgres` database, which an existing volume has to get by hand.
  - *Note, 2026-11-09:* browser errors and Web Vitals reach Loki through Grafana Faro and Alloy's `faro.receiver`, proxied same-origin by the gateway's `/telemetry/collect`, with a Browser dashboard. It fails silently while the stack is down. This is the one route from outside into this stack, so the receiver is rate limited.
