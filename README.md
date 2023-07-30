# macro-hub

## start docker database container
`docker run --name postgres-db -p 15432:5432 -e POSTGRES_PASSWORD=pgp -W`

## create database
```
psql -h localhost -p 15432 -U postgres -d pgp
CREATE DATABASE macroanalyzer;
CREATE USER macrouser WITH PASSWORD 'macropassword';
ALTER USER macrouser WITH SUPERUSER;
```

## generate jooq model
`mvn org.jooq:jooq-codegen-maven:3.18.5:generate`
