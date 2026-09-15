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
            CREATE TABLE IF NOT EXISTS accounts (
                account_id BIGSERIAL PRIMARY KEY,
                pin VARCHAR(255) NOT NULL,
                balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00
            )
            """;
    private static final String INSERT_SQL = "INSERT INTO accounts (pin, balance) VALUES (?, ?) RETURNING account_id";
    private static final String FIND_BY_ID_SQL = "SELECT account_id, pin, balance FROM accounts WHERE account_id = ?";
    private static final String UPDATE_BALANCE_SQL = "UPDATE accounts SET balance = ? WHERE account_id = ?";

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
    public void updateBalance(long accountId, BigDecimal newBalance) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_BALANCE_SQL)) {
            statement.setBigDecimal(1, newBalance);
            statement.setLong(2, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("Could not update account balance", e);
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
