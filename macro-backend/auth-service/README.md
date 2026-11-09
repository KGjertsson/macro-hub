# auth-service

Accounts, password login, sessions and the JWTs the other services verify. It serves `/macro-analyzer/auth/*` on port 8086, with its own database and role, `auth`. Why it looks like this: [ADR 0010](../../docs/adrs/0010-own-auth-service-with-sessions-and-jwts.md). The endpoints and behaviour are under "Accounts and login" in the repository's `CLAUDE.md`.

## Run it

All Maven commands run from `macro-backend/`. Without settings it uses the Compose Postgres, `localhost:5432/auth`, and writes mails to its log.

```bash
JAVA_HOME=~/.sdkman/candidates/java/current ./mvnw -pl auth-service -am spring-boot:run
```

In Compose it's the service `auth`, from the image `auth-service`:

```bash
sh ./mvnw clean package && docker build -t auth-service auth-service
```

An existing Postgres volume needs the database first. See "Starting the local stack" in `CLAUDE.md`.

## Settings for real use

Set these in `macro-backend/macro-series-service/.env`, which Compose reads:

| Variable | Meaning |
| --- | --- |
| `AUTH_PUBLIC_URL` | The site's address, which mailed links start with |
| `AUTH_ADMIN_EMAIL` | The address whose account is an admin |
| `AUTH_MAIL_MODE=smtp` | Send mail, instead of writing it to the log |
| `AUTH_MAIL_FROM` | The sender's address |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | The SMTP provider. STARTTLS is required |
| `AUTH_DB_PASSWORD` | The database role's password |

## Database

Flyway runs only this module's `db/migration`, on an empty schema. To regenerate the jOOQ model (`auth.jooq.generated`), from a database at the latest migration:

```bash
mvn -P jooq-codegen-auth compile -pl auth-service -am
```
