# macro-hub

Visualizing macro-economic trends and parameters.

### Set Up Guide

1. Start the docker database container
2. Create the database in the container
3. Run the backend application in IntelliJ
4. Install the frontend repo
5. Run the frontend repo
6. Run individual scrape queries below to populate the database

More details on the individual steps can be found below.

---

## marco-analyzer

![macro-arch.drawio.png](macro-analyzer%2Fmacro-arch.drawio.png)

### start docker database container

`docker run --name postgres-db -p 15432:5432 -e POSTGRES_PASSWORD=pgp -d postgres`

### create database

```
psql -h localhost -p 15432 -U postgres -W
CREATE DATABASE macroanalyzer;
CREATE USER macrouser WITH PASSWORD 'macropassword';
ALTER USER macrouser WITH SUPERUSER;
```

### generate jooq model

`mvn org.jooq:jooq-codegen-maven:3.18.5:generate`

### scrape data

#### Queue all data to be scraped

```
curl.exe -X POST "localhost:8080/scrape/schedule-all?interval=minute&multiplier=5"
```

#### Scrape individual datasets

```
curl.exe -X POST "localhost:8080/scrape/policy-rate/sweden"
curl.exe -X POST "localhost:8080/scrape/exchange-rate/usd-sek"
curl.exe -X POST "localhost:8080/scrape/government-bills/sweden?period=1"
curl.exe -X POST "localhost:8080/scrape/government-bills/sweden?period=3"
curl.exe -X POST "localhost:8080/scrape/government-bills/sweden?period=6"
curl.exe -X POST "localhost:8080/scrape/government-bills/sweden?period=12"
curl.exe -X POST "localhost:8080/scrape/government-bonds/sweden?period=2"
curl.exe -X POST "localhost:8080/scrape/government-bonds/sweden?period=5"
curl.exe -X POST "localhost:8080/scrape/government-bonds/sweden?period=7"
curl.exe -X POST "localhost:8080/scrape/government-bonds/sweden?period=10"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=5year&country=eur"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=5year&country=gb"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=5year&country=japan"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=5year&country=france"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=5year&country=germany"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=5year&country=netherlands"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=5year&country=usa"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=denmark"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=eur"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=finland"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=france"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=gb"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=germany"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=japan"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=netherlands"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=norway"
curl.exe -X POST "localhost:8080/scrape/government-bonds/international?period=10year&country=usa"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=3month&country=denmark"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=3month&country=eur"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=3month&country=gb"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=3month&country=japan"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=3month&country=norway"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=3month&country=usa"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=6month&country=denmark"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=6month&country=eur"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=6month&country=gb"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=6month&country=japan"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=6month&country=norway"
curl.exe -X POST "localhost:8080/scrape/euro-market-rate?period=6month&country=usa"
```

### sources

- [Riskbanken REST API](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/hamta-rantor-och-valutakurser-via-api/)
- [Riskbanken series](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/oppet-api/serier-for-webbservices/)
- [Hexagonal architecture](https://medium.com/ssense-tech/hexagonal-architecture-there-are-always-two-sides-to-every-story-bc0780ed7d9c)

## macro-frontend

NextJS + react-chartjs-2

### Sources

react-chartjs-2 documentation: https://react-chartjs-2.js.org/components/line