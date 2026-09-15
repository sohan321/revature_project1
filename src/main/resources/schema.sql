-- One-time bootstrap: creates the bankofcli role + database if they don't
-- already exist. Table creation is handled in Java by each DAO's constructor
-- (see AccountDAOImpl / TransactionDAOImpl), not here.
--
-- Run this file connected to any existing database (e.g. the server's default
-- "postgres" db) as a superuser.
--
-- Example (Docker):
--   docker exec -i <container> psql -U <superuser> -d postgres -v ON_ERROR_STOP=1 < schema.sql

DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'bankofcli') THEN
        CREATE ROLE bankofcli LOGIN PASSWORD 'bankofcli';
    END IF;
END
$$;

SELECT 'CREATE DATABASE bankofcli OWNER bankofcli'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bankofcli')\gexec
