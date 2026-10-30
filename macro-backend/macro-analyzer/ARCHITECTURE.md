# Architecture — macro-analyzer

The backend is split into modules by domain, each laid out as a small hexagon (ports and adaptors).
The full package map, the dependency rules and the checklist for a new domain are in the
"Architecture" section of the repository's `CLAUDE.md`; `ArchitectureTest` (ArchUnit) enforces the rules.

## Modules

```
  com.kg.macroanalyzer
  |
  +-- MacroAnalyzerApplication        (the only class in the root package)
  |
  +-- scrape              scrape queue, enqueue/scrape endpoints, cron jobs, the generic ScrapeFlow
  +-- macroseries         chart series: Riksbanken, Fed, Eurostat        (/chart-data, /series-config)
  +-- regionalstatistics  SCB DeSO/RegSO indicators                      (/regional-statistics/*)
  +-- parliament          Riksdagen members, activity, parties, seats    (/parliament/*)
  +-- election            Valmyndigheten kommun/region/valkrets results  (/municipalities/*, /regions/*,
  |                                                                       /constituencies/*)
  +-- shared              CORS, Jackson, JDBC, Flyway config; WebPort and the WebAdaptorFlow pipeline
  +-- jooq.generated      generated jOOQ model
```

Allowed dependencies between modules:

```
  macroseries, regionalstatistics, parliament, election --> scrape, shared, jooq.generated
  scrape                                                 --> shared, jooq.generated
  election                                               --> parliament.application.port.in (PartyDirectory)
                                                             parliament.domain (Party)
  shared                                                 --> no module
```

The election edge is the only one between domain modules. No module uses another module's out-ports,
services or adaptors.

## Inside a module

```
  DRIVING SIDE                                                              DRIVEN SIDE
                     +-------------------------------------------------+
                     |                   APPLICATION                   |
  +--------------+   |  +-----------+     +-----------+     +--------+ |   +------------------+
  | adaptors.http|-->|  | port.in   |<----| service   |---->|port.out|<|---| adaptors.postgres|--> PostgreSQL
  | (controller) |   |  | *Queries, |impl | *Service  |     | *Repo- | |   | *PostgresAdaptor |
  +--------------+   |  | DTOs      |     +-----+-----+     | sitory-| |   | + jOOQ repos     |
                     |  +-----------+           |           | Port   | |   +------------------+
                     |                          v           +--------+ |
                     |                    +-----------+                |   +------------------+
                     |                    |  domain   |    WebPort <---|---| adaptors.web     |--> external API
                     |                    | (records) |    (shared)    |   | ConnectionBuilder|
                     |                    +-----------+                |   | + ResponseParser |
                     +-------------------------------------------------+   +------------------+

  adaptors.config: *WebAdaptorConfiguration (WebPort beans), *ScrapeFlowConfiguration (KindedScrapeFlow bean)
```

- `domain` and `application` never depend on `adaptors`.
- `application.port` never depends on `application.service`, of its own module or any other (`scrape`'s included):
  ports only use domain and port types.

## ScrapeFlow pipeline (write path)

`ScrapeFlow<Input, Config, Data>` composes four steps. Each domain module wraps its flow in a
`KindedScrapeFlow` bean for one `ScrapeQueueKind`; `ScrapeService` puts them in an `EnumMap` and fails at
startup when a kind has no flow or more than one.

```
  ScrapeQueueItem (kind resolved from the config name)
       |
       v
  ScrapeService --EnumMap<ScrapeQueueKind, KindedScrapeFlow>--> flow for the item's kind
       |
       v
  1. Preparer         SeriesConfigPreparer (scrape)     looks up the SeriesConfig
  2. ExistingFinder   <module>.application.service.scrape   reads what is already stored
  3. Scraper          <module>.application.service.scrape   fetches through a WebPort
  4. Persister        <module>.application.service.scrape   writes through the module's port.out
                                                            and calls ScrapeQueuePort.markAsDone
       |
       v
  ScrapeResult (SUCCESS | FAILED | EMPTY)
```

| ScrapeQueueKind        | Flow built in                                                           |
|------------------------|-------------------------------------------------------------------------|
| `MACRO_POINT`          | `macroseries.adaptors.config.MacroPointScrapeFlowConfiguration`         |
| `MEMBER_OF_PARLIAMENT` | `parliament.adaptors.config.MemberOfParliamentScrapeFlowConfiguration`  |
| `MEMBER_ACTIVITY`      | `parliament.adaptors.config.MemberActivityScrapeFlowConfiguration`      |
| `LOCAL_ELECTION`       | `election.adaptors.config.LocalElectionScrapeFlowConfiguration`         |
| `SCB_REGIONAL`         | `regionalstatistics.adaptors.config.ScbRegionalScrapeFlowConfiguration` |

## WebAdaptorFlow pipeline

`shared.web.WebAdaptorFlow<Config, Response>` composes three steps to fetch external data:

```
  Config
       |
       v
  +--------------------+      +------------+      +-----------------+
  | ConnectionBuilder  |----->|   Getter   |----->| ResponseParser  |
  | .buildConnection() |      | .get()     |      | .parse()        |
  +--------------------+      +------------+      +-----------------+
  builds HttpURLConnection    reads response       parses to domain
  with auth headers/params    body as String        types
```

`macroseries.adaptors.web.MacroPointAdaptorFlowFactory` picks the flow by `SeriesConfig.name()`:

```
  FED*                                  --> FedConnectionBuilder        + FedResponseParser
  Debt*                                 --> EuroStatConnectionBuilder   + EuroStatResponseParser
  Int* | Euro* | GovernmentBondSweden*
  | GovernmentBillSweden* | PolicyRateSweden
  | UsdSekExchangeRate                  --> RiksbankenConnectionBuilder + RiksbankenResponseParser
```

## Data flow summary

```
  READ PATH (chart data):

  POST /chart-data --> MacroSeriesController --> MacroSeriesQueries --> MacroSeriesService
       --> MacroSeriesRepositoryPort.readMacroSeries() --> MacroSeriesPostgresAdaptor --> PostgreSQL
       --> TimeFilterService.shrinkToTimeFrame()  (TimeFrame: ALL | ONEYEAR | FIVEYEAR | TENYEAR | ONEMONTH)
       --> AlignmentService.align()
           --> LabelGenerationService.padToFullLabels()
           --> SampleStrategy.sample()  (Strategy: DAY | MONTH | YEAR)
       --> AlignedBundle --> ChartDataWithLabels (200 OK, 204 without data)

  The other read endpoints follow the same shape: <Module>Controller --> *Queries --> *Service
       --> *RepositoryPort --> *PostgresAdaptor --> PostgreSQL


  WRITE PATH (enqueue + scrape):

  POST /enqueue, /enqueue-all --> InsertionAdaptor --> InPort --> EnqueueService
       --> ScrapeQueuePort.persistScrapeQueueItem() --> ScrapeQueuePostgresAdaptor --> PostgreSQL

  POST /scrape-from-queue --> InsertionAdaptor --> InPort --> ScrapeService
  (CronScrapeAdaptor calls InPort every 60 s; CronEnqueueAdaptor calls enqueueAll during the 22:00 hour)
       --> ScrapeQueuePort.getItemsToScrape() --> PostgreSQL
       --> for each item: KindedScrapeFlow for its kind --> ScrapeFlow.run() (see above)
```
