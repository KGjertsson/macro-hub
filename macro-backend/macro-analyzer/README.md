## marco-analyzer

### Maven
generate jooq model
```
mvn -P jooq-codegen generate-sources
```

run flyway migration
```
mvn -Dflyway.url=jdbc:postgresql://localhost:31274/macroanalyzer \
    -Dflyway.user=macrouser \
    -Dflyway.password=macropassword \
    flyway:migrate
```
