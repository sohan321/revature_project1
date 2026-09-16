package com.bankofcli.persistence;

import com.bankofcli.domain.Account;
import com.bankofcli.exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAOImpl implements AccountDAO {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS account (
                account_id SERIAL PRIMARY KEY,
                pin VARCHAR(255) NOT NULL,
                balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00
            )
            """;
    private static final String INSERT_SQL = "INSERT INTO account (pin, balance) VALUES (?, ?) RETURNING account_id";
    private static final String FIND_BY_ID_SQL = "SELECT account_id, pin, balance FROM account WHERE account_id = ?";
    private static final String DEPOSIT_SQL = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
    private static final String WITHDRAW_SQL = "UPDATE account SET balance = balance - ? WHERE account_id = ? AND balance >= ?";

    public AccountDAOImpl() {
        initializeSchema();
    }

    @Override
    public Account createAccount(String pin, BigDecimal balance) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, pin);
            statement.setBigDecimal(2, balance);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new Account(resultSet.getLong("account_id"), pin, balance);
            }
        } catch (SQLException e) {
            throw databaseError("Could not create account", e);
        }
    }

    @Override
    public Account getAccountById(long accountId) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
            }
            return null;
        } catch (SQLException e) {
            throw databaseError("Could not find account", e);
        }
    }

    @Override
    public void depositFunds(long accountId, BigDecimal amount) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            depositFunds(connection, accountId, amount);
        } catch (SQLException e) {
            throw databaseError("Could not deposit funds", e);
        }
    }

    @Override
    public void depositFunds(Connection connection, long accountId, BigDecimal amount) {
        try (PreparedStatement statement = connection.prepareStatement(DEPOSIT_SQL)) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("Could not deposit funds", e);
        }
    }

    @Override
    public boolean withdrawFunds(long accountId, BigDecimal amount) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection()) {
            return withdrawFunds(connection, accountId, amount);
        } catch (SQLException e) {
            throw databaseError("Could not withdraw funds", e);
        }
    }

    @Override
    public boolean withdrawFunds(Connection connection, long accountId, BigDecimal amount) {
        try (PreparedStatement statement = connection.prepareStatement(WITHDRAW_SQL)) {
            statement.setBigDecimal(1, amount);
            statement.setLong(2, accountId);
            statement.setBigDecimal(3, amount);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw databaseError("Could not withdraw funds", e);
        }
    }

    private void initializeSchema() {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("Could not initialize accounts table", e);
        }
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getLong("account_id"),
                resultSet.getString("pin"),
                resultSet.getBigDecimal("balance"));
    }

    private DataAccessException databaseError(String message, SQLException cause) {
        return new DataAccessException(message, cause);
    }
}
