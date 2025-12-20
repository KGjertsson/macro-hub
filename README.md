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

1. Configure the microk8s cluster
2. Run postgres pod in the cluster
3. Run the backend application in IntelliJ
4. Install the frontend repo
5. Run the frontend repo
6. Run individual scrape queries below to populate the database
