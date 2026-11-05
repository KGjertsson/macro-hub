# 0008. Black-box acceptance tests through the gateway

## Status

Accepted.

## Date

2026-11-04

## Context

Each service's tests stop at its own edge. `mvn test` has unit tests and `@SpringBootTest`s with a TestContainers Postgres, and several `*HttpTest`s that call a running application over HTTP. Nothing automated covers:

- enqueue, scrape, persist and query as one journey over HTTP. No Java test names `scrape-from-queue`: only the controller, `InsertionAdaptor`, does;
- the nginx routes in `macro-frontend/nginx/default.conf.template` and their 404 fallback;
- the built images: the jar in the `Dockerfile`, `postgres-init/`, the `SPRING_DATASOURCE_*` and `*_URL` variables;
- two services running together. Election's link to parliament is held by a contract file on each side ([0005](0005-cached-rest-between-services-and-no-message-broker.md)), not by running both.

## Decision

Add acceptance tests: they start the packaged system and only talk HTTP through the gateway, asserting what a user sees, in domain terms.

- **A Maven module of its own, `macro-backend/acceptance-tests`.** It depends on no service and not on `macro-platform`, so a test can't use their classes, and it has no JDBC driver, so a test can't read the database. Black-box is enforced at compile time, and no `ArchitectureTest` is needed.
- **`*AcceptanceTest`, run by failsafe.** `mvn verify -pl acceptance-tests` runs them. Surefire is switched off in the module, because its default includes also match that name, so `mvn test` and CI's `backend` job don't start the stack.
- **Testcontainers `GenericContainer`s on a shared network, not `docker-compose-all.yml`.** That file has fixed ports, `container_name`s and the Compose project `docker`, so it would collide with a running local stack and could share `docker_pgdata`, which holds the only copy of `parliament`. Here the ports are random and the database is thrown away.
- **The real parts.** `postgres:16` with the real `postgres-init/` scripts, the real service images, and the frontend's nginx as the gateway. All four services run, started in parallel. Most journeys share one stack, each with its own series or area. A journey that needs clean or unusual state starts its own with `AcceptanceStack.startWith`, with the services it chooses. nginx resolves an upstream per request, so in a stack without a service, that service's routes answer 502 and the rest keep working, which `AbsentServiceAcceptanceTest` pins. A service started later with `startLater`, under the same alias, is reached without touching the gateway, which is how `PartyColoursAcceptanceTest` runs election without parliament and then with it.
- **The external sources are WireMock in a container**, serving fixtures from `src/test/resources/sources/`. Nothing calls a real source.
- **No production code changes to reach the stub.** `series_configuration.scrape_url` is seeded by migrations and read once at start-up, so the stack starts each service to let Flyway seed, rewrites the host of every row to the stub with `psql` inside the Postgres container, and starts the service again. Setup fails if a row still points elsewhere. This is the only place the harness touches the database, and it isn't used for assertions. `SCHEDULER_ENABLED=false` keeps the crons from draining the queue during a test or answering it with 409.
- **Images tagged `:acceptance`.** `macro-infra/scripts/acceptance-test.sh` packages the jars, builds them and runs the tests, in CI and locally, so the images a run uses are never stale and what a local Compose stack starts doesn't change.
- **A separate `acceptance` CI job, not a required check.** The ruleset isn't changed: `frontend` and `backend` stay the required ones. A failing `acceptance` shows on the PR without blocking the merge.
- **A separate `compose-smoke` CI job for `docker-compose-all.yml` itself, also not required.** Testcontainers can't use that file, so `macro-infra/scripts/compose-smoke.sh` starts it on a runner, where its fixed ports and project name are free, on an empty volume with `.env.example` as `.env`. It checks each service's route through the gateway, with the seeded counts, and the gateway's 404. The script refuses to run without `CI=true`, because on a developer's machine it would collide with the local stack and could share `docker_pgdata`.

The suite has journeys for each domain's scrape and read through the gateway, the gateway's routes and fallback, what a fresh system serves, a service that is absent, the colours election reads from parliament, and failure and recovery: a failing source, a second drain during a scrape, and a Postgres restart.

## Alternatives considered

- **Cucumber or Gherkin.** Its point is specifications that non-developers read, and this project has none: it would only add glue code between the sentences and the same assertions. Tests use `// given`, `// when` and `// then`, with small helpers (`Gateway`, `Sources`).
- **Reusing `docker-compose-all.yml`, through Testcontainers' Compose support or a project name.** The fixed `container_name`s and ports would still collide with a running stack. Overriding them leaves a second file that has to be kept in step anyway.
- **A property or environment variable for the source's host.** It would be simpler than starting twice, but a production code path that only tests use, and each service has several sources.
- **JDBC from the harness for the redirect.** It would put a driver on the test classpath, where a later test could use it for an assertion. `psql` in the container needs none.
- **Testing against the real sources.** Slow, and it would fail when a source is down or a value is revised. See the consequences.

## Consequences

- Stubs can drift from the real sources: a changed response format still passes here. A nightly job against the real sources would close that. There isn't one.
- Feedback is slower than `mvn test`: a run of the tests takes about 6 minutes in CI with the images built, nearly all of it starting thirteen stacks, because each service starts twice. Each journey adds to it, and a journey with its own stack adds most, so the suite stays small: journeys a user would recognise, not every case, which stay in each service's tests.
- `AcceptanceStack` still doesn't run `docker-compose-all.yml`, so a variable changed there and not in `AcceptanceStack` isn't seen by the journeys. `compose-smoke` shows that the file starts and that every service answers its route, but it scrapes nothing, so what only a scrape uses there, such as the subscription keys, goes untested. It can't be run locally, so it is tried through a PR.
- A `compose-smoke` run in the 22:00 UTC hour has the crons on, as any Compose stack does, so they may call the real sources for as long as the job runs. Its checks hold either way.
- Because `acceptance` and `compose-smoke` aren't required, they can fail on a merged PR. Whoever reviews a PR has to read them. Neither has been made required.
- A service's first start in a test uses the real URLs in its seeded rows. Nothing calls a source at start-up today, with the scheduler off. A service that did would reach the real source during setup.
- A new service joins by adding a `BackendService` constant, with its other start-up settings such as election's `ELECTION_PARLIAMENT_URL`, putting it in `BackendService.ALL`, and adding a `docker build` line to the script, and to `compose-smoke.sh` with a check of one of its routes.
