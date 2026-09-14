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

\c bankofcli

SET ROLE bankofcli;

CREATE TABLE IF NOT EXISTS accounts (
    account_id      BIGSERIAL PRIMARY KEY,
    pin             VARCHAR(255) NOT NULL,
    balance         NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      BIGSERIAL PRIMARY KEY,
    account_id           BIGINT NOT NULL REFERENCES accounts(account_id),
    type                 VARCHAR(20) NOT NULL,
    amount               NUMERIC(12, 2) NOT NULL,
    related_account_id   BIGINT REFERENCES accounts(account_id),
    created_at           TIMESTAMP NOT NULL DEFAULT NOW()
);
