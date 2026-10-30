## marco-analyzer

Java backend application for macro-hub. Built using domain driven design principles and the
hexagonal architecture, split into one module per domain (`macroseries`, `regionalstatistics`,
`parliament`, `election`) plus the `scrape` core and `shared` infrastructure. See
[ARCHITECTURE.md](ARCHITECTURE.md) and the "Architecture" section of the repository's `CLAUDE.md`.

Inside every module:

```mermaid
flowchart LR
    Controller[adaptors.http] --> PortIn[application.port.in]
    subgraph M[Module]
        PortIn --> Service[application.service]
        Service --> domain{{domain}}
        Service --> PortOut[application.port.out]
    end
    PostgresAdaptor[adaptors.postgres] -. implements .-> PortOut
    PostgresAdaptor --> DB[(PostgreSQL)]
    WebAdaptor[adaptors.web] -. implements .-> WebPort[shared.web.WebPort]
    Service --> WebPort
    WebAdaptor --> API[External API]
```

Scraping, shared by all modules:

```mermaid
flowchart LR
    InsertionAdaptor --> InPort
    CronScrapeAdaptor --> InPort
    CronEnqueueAdaptor --> InPort
    InPort --> EnqueueService --> ScrapeQueuePort
    InPort --> ScrapeService
    ScrapeService -- ScrapeQueueKind --> KindedScrapeFlow
    KindedScrapeFlow --> Preparer --> ExistingFinder --> Scraper --> Persister
    Persister --> RepositoryPort[module port.out]
    Persister --> ScrapeQueuePort
```

### Data Model

```mermaid
erDiagram
    SERIES_CONFIG ||--|| MACRO_SERIES : configures
    SERIES_CONFIG ||--}| MEMBERS_OF_PARLIAMENT : configures
    
    SERIES_CONFIG {
        integer id
        uuid global_id
        string name
        string display_name
        datetime created
        datetime updated
        string scrape_url
        string category
        string unit
        string subcategory
        string country
        integer maturity_months
    }
    
    MACRO_SERIES {
        integer id
        uuid global_id
        string series_name
        double value
        datetime date
        datetime created
        datetime updated
    }
    
    MEMBERS_OF_PARLIAMENT {
        integer id
        uuid global_id
        string member_name
        jsonb member_data
        datetime created
        datetime updated
    }
```

### Sources

- [Riskbanken REST API](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/hamta-rantor-och-valutakurser-via-api/)
- [Riskbanken series](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/oppet-api/serier-for-webbservices/)
- [Hexagonal architecture](https://medium.com/ssense-tech/hexagonal-architecture-there-are-always-two-sides-to-every-story-bc0780ed7d9c)
- [Commit message guidelines](https://gist.github.com/robertpainsi/b632364184e70900af4ab688decf6f53)
- [Kafka with microk8s](https://medium.com/@prasanta.mohanty/deploy-kafka-cluster-on-microk8s-in-15-mins-f3d5081991e8)

### Maven

generate jooq model

```
mvn -P jooq-codegen generate-sources
```

Flyway migrations (`src/main/resources/db/migration/`) run on application startup; there is no Flyway
Maven plugin.
