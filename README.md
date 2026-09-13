# Bank of CLI

A terminal-based banking application built with Java, Maven, and PostgreSQL.
See [P0-Instructions.md](P0-Instructions.md) for the full project spec.

## Prerequisites

- Java 17 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Docker Desktop (for running PostgreSQL locally)

## Local database setup

The project's PostgreSQL database runs in Docker, defined in [docker-compose.yml](docker-compose.yml).
This works the same way on any machine with Docker Desktop installed and running.

```
docker compose up -d
```

This starts a `postgres:16` container named `bankofcli-postgres` with:

- Database: `bankofcli`
- User: `bankofcli`
- Password: `bankofcli`
- Port: `5432` (mapped to localhost)

On first startup only, it automatically runs [src/main/resources/schema.sql](src/main/resources/schema.sql)
to create the `accounts` and `transactions` tables.

Connection details the app uses are in [src/main/resources/application.properties](src/main/resources/application.properties).

To stop the container (keeping data): `docker compose stop`
To stop and remove it (keeping data volume): `docker compose down`
To wipe the database entirely and start fresh: `docker compose down -v`

## Building and running

```
mvn compile      # compile the project
mvn test         # run the JUnit 5 test suite
mvn package      # build the runnable jar into target/
```
