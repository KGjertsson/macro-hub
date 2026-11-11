# Architecture diagram

The components that run, and how they connect. It is what Docker Compose starts
(`macro-backend/macro-infra/docker/docker-compose-all.yml`).

```
  +---------------------+     direct from the browser, not through the gateway:
  | Browser             |--+---> OpenFreeMap   map style and tiles, on /omraden
  | runs the React app  |  +---> Photon        address search, on /omraden
  +---------------------+  +---> Google Fonts  Inter and Space Mono, on every page
            |
            | http://localhost:8081
            v
  +--------------------------------------------------------------------------------------------------------+
  | macro-frontend container: nginx, host :8081 to container :80                                           |
  | serves the React app's files and is the gateway, with the routes listed below.                         |
  +--------------------------------------------------------------------------------------------------------+
    |                   |                        |                      |                   |
    v                   v                        v                      v                   v
  +----------------+  +---------------------+  +----------------+     +----------------+  +----------------+
  | macro-series   |  | regional-statistics |  | parliament     |     | election       |  | auth           |
  | :8083          |  | :8082               |  | :8084          |<-(1)| :8085          |  | :8086          |
  | cron, queue    |  | cron, queue         |  | cron, queue    |     | cron, queue    |  | accounts,      |
  | - - - - - - -  |  | - - - - - - - - - - |  | - - - - - - -  |     | - - - - - - -  |  | sessions, JWTs |
  | macro-platform |  | macro-platform      |  | macro-platform |     | macro-platform |  | - - - - - - -  |
  +-+--------------+  +-+-------------------+  +-+--------------+     +-+--------------+  | shared only    |
    | |                 | |                      | |                    | |               +-+--------------+
    | |                 | |                      | |                    | |                 | |
    | v                 | v                      | v                    | v                 | v
    | Riksbanken        | SCB                    | Riksdagen            | Valmyndigheten    | SMTP provider
    | Fed (FRED)        |                        |                      |                   | (or its log)
    | Eurostat          |                        |                      |                   |
    |                   |                        |                      |                   |
    v                   v                        v                      v                   v
  +--------------------------------------------------------------------------------------------------------+
  | postgres container, one instance: host :5432, volume docker_pgdata                                     |
  | macro_series        regional_statistics      parliament             election            auth           |
  | Each scraping service's database has its own role, series_configuration and scrape_action_queue.       |
  | auth's has its own role, the accounts, sessions, mailed-link tokens and the signing key.               |
  +--------------------------------------------------------------------------------------------------------+

  . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
  . macro-platform: a library, not a running component. It is built into each service's jar, and          .
  . gives the scraping services the scrape queue, the cron jobs, the scrape/ endpoints, the ScrapeFlow    .
  . and the JWT check on scrape/. auth uses only its shared code.                                         .
  . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  Routes, all under /macro-analyzer/ (any other path there answers 404):
    macro-series         series-config, chart-data, scrape/macro-series/
    regional-statistics  regional-statistics/, scrape/regional-statistics/
    parliament           parliament/, scrape/parliament/
    election             municipalities/, regions/, constituencies/, scrape/election/
    auth                 auth/, at most 10 requests per minute per client address, except token,
                         logout and jwks.json
  Under a service: the external services it calls, over HTTPS (SMTP with STARTTLS).
  (1) election reads the party colours from parliament directly, at http://parliament:8084:
      GET /macro-analyzer/parliament/parties, cached in memory, refreshed hourly in the background.
  (2) Not drawn as lines: the four scraping services fetch auth's public keys directly, at
      http://auth:8086/macro-analyzer/auth/jwks.json, at the first token on a scrape/ path. They check
      every JWT with those keys and call auth for nothing else. (1) and (2) are the only runtime calls
      between services. See authentication.md.
  cron: drains the service's queue every 60 s, and enqueues all its series every minute of the
        22:00 hour.
```

A box's name is its Compose service, and a service's Maven module is that name plus `-service`.

The services and Postgres also publish their ports on the host, for local work; auth's only on
`127.0.0.1`. With `npm run dev`, Vite on `:5173` takes nginx's place, with the same routes but without
the rate limit, and calls the services on `localhost`.

- How login and the scrape endpoints' protection work: [`docs/authentication.md`](authentication.md)
- Inside a module: [`macro-backend/ARCHITECTURE.md`](../macro-backend/ARCHITECTURE.md)
- Why it looks like this: [`docs/adrs/README.md`](adrs/README.md)
