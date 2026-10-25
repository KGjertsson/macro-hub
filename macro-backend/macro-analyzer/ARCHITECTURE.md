# Hexagonal Architecture — macro-analyzer

## Overview

```
  DRIVING SIDE                                                         DRIVEN SIDE
  (primary)                                                            (secondary)
                         +-----------------------------------------------+
                         |              APPLICATION CORE                 |
  +-----------------+    |                                               |    +-------------------+
  |                 |    |  +-----------+         +-----------+          |    |                   |
  | Insertion       |--->|  |  InPort   |-------->| InPortImpl|          |    |                   |
  |  Adaptor        |    |  +-----------+         +-----+-----+          |    |                   |
  |  (REST)         |    |   enqueue()              |       |            |    |                   |
  |                 |    |   enqueueAll()            v       v           |    |                   |
  | POST /enqueue   |    |   scrapeFromQueue()  Enqueue  Scrape          |    |                   |
  | POST /enqueue-  |    |                      Service  Service         |    |                   |
  |       all       |    |                         |       |            |    |  PostgresAdaptor  |
  | POST /scrape-   |    |                         |       |            |    |  (DatabasePort)   |
  |       from-     |    |                         |   +---+----+       |    |                   |
  |       queue     |    |                         |   |ScrapeFlow|      |    |  10 Repositories: |
  +-----------------+    |                         |   | .run()   |      |    |  - ExchangeRate   |
                         |                         |   +---+------+      |    |  - GovBonds       |
  +-----------------+    |                         |       |             |    |  - GovBills       |
  |                 |    |                     +---v-------v---+         |    |  - PolicyRate     |
  | Extraction      |--->|  +-----------+      | DatabasePort  |-------->|--->|  - EuroMarketRate |
  |  Adaptor        |    |  |  OutPort  |      +---------------+         |    |  - Fed            |
  |  (REST)         |    |  +-----------+       readMacroSeries()       |    |  - Eurostat       |
  |                 |    |   buildAligned        readSeriesConfig()      |    |  - SweParMember   |
  | POST /chart-    |    |    Bundle()           writeMacroPoints()      |    |  - Scrape         |
  |       data      |    |   getSeriesConfig     persistScrapeQueue()    |    |  - SeriesConfig   |
  | GET  /series-   |    |    List()             getItemsToScrape()      |    |                   |
  |       config    |    |        |              markAsDone()            |    +--------+----------+
  +-----------------+    |        v                                      |             |
                         |  +-----------+                                |             v
  +-----------------+    |  |OutPortImpl|                                |    +--------+----------+
  |                 |    |  +-----+-----+                                |    |   PostgreSQL 16   |
  | CronEnqueue     |--->|        |                                      |    |   (JOOQ+Flyway)   |
  |  Adaptor        |    |   +----+--------+                             |    +-------------------+
  |  (@Scheduled)   |    |   |    |        |                             |
  +-----------------+    |   v    v        v                             |
                         |  Time  Align  Label                           |
  +-----------------+    |  Filter ment  Generation                     |    +-------------------+
  |                 |    |  Svc   Svc    Svc                            |    |                   |
  | CronScrape      |--->|                                              |    | MacroPoint        |
  |  Adaptor        |    |  +----------------------------------+        |    |  WebAdaptor       |
  |  (@Scheduled)   |    |  |         DOMAIN MODEL             |        |    |  (WebPort)        |
  +-----------------+    |  |                                  |        |    |                   |
                         |  |  MacroPoint(value, date)         |        |    | uses Adaptor      |
                         |  |  MacroSeries(name, macroPoints)  |        |    |  FlowFactory:     |
                         |  |  AlignedBundle(series, labels)   |        |    |                   |
                         |  |  MemberOfParliament              |  +----------+  FED* -->         |
                         |  |  ScrapeQueueItem                 |  |     |    |   FedConnection   |
                         |  |  ScrapeQueueKind:                |  |     |    |   Builder +       |
                         |  |    MACRO_POINT                   |  |     |    |   FedResponse     |
                         |  |    MEMBER_OF_PARLIAMENT          |  |     |    |   Parser          |
                         |  |                                  |  |     |    |                   |
                         |  +----------------------------------+  |     |    |  Debt* -->        |
                         |                                        |     |    |   EuroStatConn    |
                         |              +----------+              |     |    |   Builder +       |
                         |              | WebPort  |--------------+     |    |   EuroStatResp    |
                         |              +----------+                    |    |   Parser          |
                         |               fetch()                        |    |                   |
                         |                                              |    |  Int*/Euro*/      |
                         +-----------------------------------------------+    |  PolicyRate -->  |
                                                                        |    |   RiksbankenConn |
                                                                        |    |   Builder +      |
                                                                        |    |   RiksbankenResp |
                                                                        |    |   Parser         |
                                                                        |    +--------+---------+
                                                                        |             |
                                                                        |    +--------v---------+
                                                                        |    | MembersOfParlia- |
                                                                        |    |  mentWebAdaptor  |
                                                                        |    |  (WebPort)       |
                                                                        |    |                  |
                                                                        |    | RiksdagenConn    |
                                                                        |    |  Builder +       |
                                                                        |    | RiksdagenResp    |
                                                                        |    |  Parser          |
                                                                        |    +--------+---------+
                                                                        |             |
                                                                        |             v
                                                                        |    +-------------------+
                                                                        |    | EXTERNAL APIs     |
                                                                        |    |                   |
                                                                        |    | - Riksbanken      |
                                                                        |    | - Federal Reserve |
                                                                        |    | - Eurostat        |
                                                                        |    | - Riksdagen       |
                                                                        |    +-------------------+
```

## ScrapeFlow Pipeline (Write Path)

The `ScrapeFlow<Input, Config, Data>` is a generic record composing four pluggable steps.
`ScrapeService` selects the correct flow instance via `ScrapeQueueKind`:

```
                         macroPointScrapeFlow                          memberScrapeFlow
                         (MACRO_POINT)                                 (MEMBER_OF_PARLIAMENT)
                         ====================                          ====================

  ScrapeQueueItem -----> SeriesConfigPreparer -----> SeriesConfigPreparer
                              |                           |
                              v                           v
                         ExistingMacroPoint          ExistingMemberOf
                          Finder                     ParliamentFinder
                          (DatabasePort)             (stub/TODO)
                              |                           |
                              v                           v
                         MacroPointScraper           MemberOfParliament
                          (WebPort)                  Scraper (WebPort)
                              |                           |
                              v                           v
                         MacroPointPersister         MemberOfParliament
                          (DatabasePort)             Persister (TODO)
                              |                           |
                              v                           v
                         ScrapeResult                ScrapeResult
                         (SUCCESS/EMPTY)             (FAILED — not yet wired)
```

## WebAdaptorFlow Pipeline

Each `WebAdaptorFlow<Config, Response>` composes three steps to fetch external data:

```
  SeriesConfig
       |
       v
  +--------------------+      +------------+      +-----------------+
  | ConnectionBuilder  |----->|   Getter   |----->| ResponseParser  |
  | .buildConnection() |      | .get()     |      | .parse()        |
  +--------------------+      +------------+      +-----------------+
  builds HttpURLConnection    reads response       parses to domain
  with auth headers/params    body as String        Stream<MacroPoint>
```

Routing by `SeriesConfig.name()` prefix:

```
  FED*                  --> FedConnectionBuilder      + FedResponseParser
  Debt*                 --> EuroStatConnectionBuilder  + EuroStatResponseParser
  Int* | Euro* | Policy --> RiksbankenConnectionBuilder + RiksbankenResponseParser
  (parliament)          --> RiksdagenConnectionBuilder  + RiksdagenResponseParser
```

## Data Flow Summary

```
  READ PATH (chart data):

  POST /chart-data --> ExtractionAdaptor --> OutPort --> OutPortImpl
       --> DatabasePort.readMacroSeries() --> PostgresAdaptor --> PostgreSQL
       --> TimeFilterService.shrinkToTimeFrame()
       --> AlignmentService.align()
           --> LabelGenerationService.padToFullLabels()
           --> SampleStrategy.sample()  (DAY | MONTH | YEAR)
       --> AlignedBundle --> ChartDataWithLabels (200 OK)


  WRITE PATH (enqueue + scrape):

  POST /enqueue --> InsertionAdaptor --> InPort --> EnqueueService
       --> DatabasePort.persistScrapeQueueItem() --> PostgreSQL

  POST /scrape-from-queue --> InsertionAdaptor --> InPort --> ScrapeService
       --> DatabasePort.getItemsToScrape() --> PostgreSQL
       --> for each item:
           selectScrapeFlow(ScrapeQueueKind) --> ScrapeFlow.run():
             1. Preparer:  SeriesConfigPreparer (lookup config)
             2. Finder:    read existing from DatabasePort --> PostgreSQL
             3. Scraper:   WebPort.fetch() --> WebAdaptorFlow --> External API
             4. Persister: DatabasePort.write() + markAsDone() --> PostgreSQL
```

## Dependency Rule

All dependencies point **inward** toward the domain:
- Driving adaptors depend on driving ports (InPort, OutPort)
- Application services implement driving ports and depend on driven ports
- Driven adaptors implement driven ports (DatabasePort, WebPort)
- The domain model has **zero** external dependencies
