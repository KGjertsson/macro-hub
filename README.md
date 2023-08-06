# macro-hub

Visualizing macro-economic trends and parameters.

---

## marco-analyzer

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

```
curl.exe -X POST localhost:8080/scrape/policy-rate/sweden
curl.exe -X POST localhost:8080/scrape/exchange-rate/usd-sek
```

---

## macro-frontend

react-chartjs-2 documentation: https://react-chartjs-2.js.org/components/line