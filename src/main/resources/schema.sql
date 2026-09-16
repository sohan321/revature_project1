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


