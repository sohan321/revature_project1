package com.bankofcli.persistence;

import com.bankofcli.domain.Transaction;
import com.bankofcli.domain.TransactionType;
import com.bankofcli.exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id BIGSERIAL PRIMARY KEY,
                account_id BIGINT NOT NULL REFERENCES accounts(account_id),
                type VARCHAR(20) NOT NULL,
                amount NUMERIC(12, 2) NOT NULL,
                related_account_id BIGINT REFERENCES accounts(account_id),
                created_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """;
    private static final String INSERT_SQL = "INSERT INTO transactions (account_id, type, amount, related_account_id) " + "VALUES (?, ?, ?, ?) RETURNING transaction_id, created_at";
    private static final String FIND_BY_ACCOUNT_ID_SQL = "SELECT transaction_id, account_id, type, amount, " + "related_account_id, created_at FROM transactions WHERE account_id = ? ORDER BY created_at DESC";

    public TransactionDAOImpl() {
        initializeSchema();
    }

    @Override
    public Transaction createTransaction(long accountId, TransactionType type, BigDecimal amount, Long relatedAccountId) {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, accountId);
            statement.setString(2, type.name());
            statement.setBigDecimal(3, amount);
            if (relatedAccountId == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, relatedAccountId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                long transactionId = resultSet.getLong("transaction_id");
                var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                return new Transaction(transactionId, accountId, type, amount, relatedAccountId, createdAt);
            }
        } catch (SQLException e) {
            throw databaseError("Could not create transaction", e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(long accountId) {
        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ACCOUNT_ID_SQL)) {
            statement.setLong(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw databaseError("Could not list transactions", e);
        }
    }

    private void initializeSchema() {
        try (Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
                PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("Could not initialize transactions table", e);
        }
    }

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        long relatedAccountId = resultSet.getLong("related_account_id");
        Long relatedAccountIdOrNull = resultSet.wasNull() ? null : relatedAccountId;

        return new Transaction(
                resultSet.getLong("transaction_id"),
                resultSet.getLong("account_id"),
                TransactionType.valueOf(resultSet.getString("type")),
                resultSet.getBigDecimal("amount"),
                relatedAccountIdOrNull,
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }

    private DataAccessException databaseError(String message, SQLException cause) {
        return new DataAccessException(message, cause);
    }
}
