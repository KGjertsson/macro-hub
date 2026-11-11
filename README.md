# macro-hub

Visualizing macro-economic trends and parameters.

## Data Sources

### Eurostat

Each line represents a combination of dimensions describing a specific economic indicator. Let’s
break down the components:
Structure of Each Identifier
freq, unit, sector, na_item, geo

#### freq

A = Annual frequency (data reported yearly).

#### unit

MIO_EUR = Millions of euros.  
MIO_NAC = Millions in national currency.  
PC_GDP = Percentage of GDP.

#### sector

S1 = Total economy.  
S13 = General government.  
S1311 = Central government.

#### na_item (National Accounts Item)

These codes represent specific economic indicators:

B1GQ = Gross Domestic Product (GDP) at market prices.  
B5GQ = Gross National Income (GNI).  
B9 = Net lending (+) / net borrowing (-).  
AF_81L = Liabilities (financial assets).  
D41PAY = Interest payable.  
GD = Government debt.  
GD_F2 = Government debt in currency and deposits.  
GD_F42 = Government debt in loans.  
IGL_F4_EU27_2020 = Intergovernmental lending within EU27.  
P51G = Gross fixed capital formation.

#### geo

Country or region codes:

AT = Austria, BE = Belgium, BG = Bulgaria, etc.  
EA19 = Euro area (19 countries).  
EA20 = Euro area (20 countries).  
EU27_2020 = European Union (27 countries as of 2020).

- [Eurostat](https://ec.europa.eu/eurostat/web/user-guides/data-browser/api-data-access/api-detailed-guidelines/api-statistics)

## Set Up Guide

The backend is five services and a shared library, in `macro-backend/`, and the dashboard is in `macro-frontend/`. See `CLAUDE.md` for the architecture and all the ways to run them, and `macro-backend/ARCHITECTURE.md` for diagrams.

1. `cp macro-backend/macro-series-service/.env.example macro-backend/macro-series-service/.env`. The keys in it are only needed to scrape Riksbanken and Fed series
2. Build the backend images: `(cd macro-backend && ./mvnw clean package && docker build -t regional-statistics-service regional-statistics-service && docker build -t macro-series-service macro-series-service && docker build -t parliament-service parliament-service && docker build -t election-service election-service && docker build -t auth-service auth-service)`
3. Start everything: `docker compose --env-file macro-backend/macro-series-service/.env -f macro-infra/docker/docker-compose-all.yml up -d --build`, and open `http://localhost:8081`
4. Run `macro-infra/scripts/scrape-all.sh` to populate the databases, which takes about 8 minutes

`docs/adrs/` records the larger decisions, and `docs/adding-a-service.md` says how to add a service.

## Sources

- [Riksbanken REST API](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/hamta-rantor-och-valutakurser-via-api/)
- [Riksbanken series](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/oppet-api/serier-for-webbservices/)
- [Hexagonal architecture](https://medium.com/ssense-tech/hexagonal-architecture-there-are-always-two-sides-to-every-story-bc0780ed7d9c)
- [Commit message guidelines](https://gist.github.com/robertpainsi/b632364184e70900af4ab688decf6f53)
