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
  +-----------------------------------------------------------------------------------------------+
  | macro-frontend container: nginx, host :8081 to container :80                                  |
  | serves the React app's files and is the gateway. Routes below are under /macro-analyzer/;     |
  | any other path there answers 404.                                                             |
  +-----------------------------------------------------------------------------------------------+
    |                      |                               |                      |
    | series-config        | regional-statistics/          | parliament/          | municipalities/
    | chart-data           | scrape/regional-statistics/   | scrape/parliament/   | regions/
    | scrape/macro-series/ |                               |                      | constituencies/
    |                      |                               |                      | scrape/election/
    v                      v                               v                      v
  +------------------+   +---------------------+         +----------------+     +----------------+
  | macro-series     |   | regional-statistics |         | parliament     |     | election       |
  | :8083            |   | :8082               |         | :8084          |<-(1)| :8085          |
  | cron, queue      |   | cron, queue         |         | cron, queue    |     | cron, queue    |
  | - - - - - - - -  |   | - - - - - - - - - - |         | - - - - - - -  |     | - - - - - - -  |
  | macro-platform   |   | macro-platform      |         | macro-platform |     | macro-platform |
  +-+-+--------------+   +-+-+-----------------+         +-+-+------------+     +-+-+------------+
    | |                    | |                             | |                    | |
    | v                    | v                             | v                    | v
    | Riksbanken           | SCB                           | Riksdagen            | Valmyndigheten
    | Fed (FRED)           |                               |                      |
    | Eurostat             |                               |                      |
    |                      |                               |                      |
    v                      v                               v                      v
  +-----------------------------------------------------------------------------------------------+
  | postgres container, one instance: host :5432, volume docker_pgdata                            |
  | macro_series           regional_statistics             parliament             election        |
  | Each database has its own role, series_configuration and scrape_action_queue.                 |
  +-----------------------------------------------------------------------------------------------+

  . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
  . macro-platform: a library, not a running component. It is built into each service's jar,      .
  . and gives it the scrape queue, the cron jobs, the scrape/ endpoints and the ScrapeFlow.       .
  . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  Under a service: the external APIs it scrapes, over HTTPS.
  (1) election reads the party colours from parliament directly, at http://parliament:8084:
      GET /macro-analyzer/parliament/parties, cached in memory, refreshed hourly in the background.
      It is the only runtime call between services.
  cron: drains the service's queue every 60 s, and enqueues all its series every minute of the
        22:00 hour.
```

A box's name is its Compose service, and a service's Maven module is that name plus `-service`.

The services and Postgres also publish their ports on the host, for local work. With `npm run dev`,
Vite on `:5173` takes nginx's place, with the same routes, and calls the services on `localhost`.

- Inside a module: [`macro-backend/ARCHITECTURE.md`](../macro-backend/ARCHITECTURE.md)
- Why it looks like this: [`docs/adrs/README.md`](adrs/README.md)
