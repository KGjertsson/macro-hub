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

1. Start Postgres with `docker compose -f macro-backend/macro-infra/docker/docker-compose-dependencies.yml up -d`. It listens on `localhost:5432`, the backend's default
2. Run the backend application in IntelliJ
3. Install the frontend repo
4. Run the frontend repo
5. Run `macro-backend/macro-infra/scripts/scrape-all.sh` to populate the databases

The backend is being split into one service per domain. See `CLAUDE.md` for what has moved and the current way to start the stack, and `docs/microservice-migration-guide.md` for the plan.

To run the backend against the microk8s cluster's database instead (see `macro-backend/macro-infra/k8s/README.md`), set `SPRING_DATASOURCE_URL` to it. Nothing defaults to it.
