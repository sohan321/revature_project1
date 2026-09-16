# Bank of CLI

A terminal-based banking application built with Java, Maven, and PostgreSQL.
See [P0-Instructions.md](P0-Instructions.md) for the full project spec.

## Prerequisites

- Java 17 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- A running PostgreSQL server, reachable as a superuser (e.g. via Docker)

## Local database setup

The app connects to a `bankofcli` database using its own `bankofcli` role. Table creation is
handled automatically by the app itself (see below) — you only need to bootstrap the role and
database once per machine.

**1. Create the role + database** (one-time, run as a superuser):

```
docker exec -i <your-postgres-container> psql -U <superuser> -d <any-existing-db> -v ON_ERROR_STOP=1 < src/main/resources/schema.sql
```

This creates a `bankofcli` role (password `bankofcli`) and a `bankofcli` database, matching the
credentials in [src/main/resources/db.properties](src/main/resources/db.properties). It's
idempotent — safe to run again later without error.

**2. Tables are created automatically.** `AccountDAOImpl` and `TransactionDAOImpl` each run
`CREATE TABLE IF NOT EXISTS` in their constructor, so the `account` and `transaction` tables get
created the first time the app (or the test suite) actually runs — no separate migration step.

**Resetting data for a fresh testing session** (wipes rows, keeps the schema, resets IDs back to 1):

```
docker exec -i <your-postgres-container> psql -U bankofcli -d bankofcli -v ON_ERROR_STOP=1 < src/main/resources/reset.sql
```

## Building and running

```
mvn compile      # compile the project
mvn test         # run the JUnit 5 test suite (uses the real database above)
mvn package      # build the runnable jar into target/
```
