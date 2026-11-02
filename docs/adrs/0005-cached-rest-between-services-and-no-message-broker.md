# 0005. Cached REST between services and no message broker

## Status

Accepted

## Date

2026-11-01

## Context

Only one runtime dependency crosses domains. `LocalElectionService` calls `PartyDirectory.listParties()` to build each response, so that a Riksdag party has the same colour on every page. If a party isn't in that list, it uses Valmyndigheten's colour, and then the fallback `#9CA3AF`.

The data barely changes. `riksdag_party` is only written by Flyway migrations (V22 and V24 so far), and no scraper touches it. `GET /macro-analyzer/parliament/parties` already returns it. It answers 204 when the list is empty.

No service has to react to another's writes. `kubernetes_setup.sh` still installs Kafka, but no application code or dependency uses it.

## Decision

Election gets its own out-port in place of `PartyDirectory`, with an HTTP adaptor that calls `GET /parliament/parties` in the background and keeps the party list in memory. The refresh interval isn't fixed yet; about an hour is enough for this data.

- If parliament is down, election keeps the last list.
- If there is no list yet, election uses Valmyndigheten's colour, as it does for local parties today.

A contract test pins the fields election reads (`code` and `color`). There is no message broker.

*Note, 2026-11-02:* the Flyway versions in the context were the monolith's, now gone from the tree. `riksdag_party` is written by `parliament-service`'s baseline and its later migrations. Election runs as `election-service`, and the behaviour here is unchanged.

## Alternatives considered

- **Call parliament on every request.** This is the simplest, but a parliament outage would fail or slow election requests.
- **Election keeps its own copy of the colours.** This removes the call, but a new party or colour would need two migrations.
- **Message broker with events, such as Kafka.** Rejected for now. It would be the largest process in the cluster, with one low-value message to carry. If a service ever has to react to another's writes, add a transactional outbox and a broker then.

## Consequences

- A parliament outage never fails an election request. The worst case is the wrong colour after a cold start, such as SD in Valmyndigheten's blue.
- Colour changes reach election up to one refresh interval late.
- The `election` entry in `DOMAIN_MODULE_EXTRA_DEPENDENCIES` goes away, so `ArchitectureTest` no longer needs an exception.
- `/parliament/parties` becomes an API between services, so changing its fields needs care.
