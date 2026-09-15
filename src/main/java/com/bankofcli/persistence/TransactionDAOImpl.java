package com.bankofcli.persistence;

import com.bankofcli.domain.Transaction;
import com.bankofcli.domain.TransactionType;
import com.bankofcli.exception.DataAccessException;
import com.bankofcli.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public Transaction createTransaction(long accountId, TransactionType type, BigDecimal amount, Long relatedAccountId) {
        String sql = "INSERT INTO transactions (account_id, type, amount, related_account_id) "
                + "VALUES (?, ?, ?, ?) RETURNING transaction_id, created_at";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);
            stmt.setString(2, type.name());
            stmt.setBigDecimal(3, amount);
            if (relatedAccountId == null) {
                stmt.setNull(4, Types.BIGINT);
            } else {
                stmt.setLong(4, relatedAccountId);
            }

            ResultSet rs = stmt.executeQuery();
            rs.next();

            long transactionId = rs.getLong("transaction_id");
            LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

            return new Transaction(transactionId, accountId, type, amount, relatedAccountId, createdAt);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create transaction", e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(long accountId) {
        String sql = "SELECT transaction_id, account_id, type, amount, related_account_id, created_at "
                + "FROM transactions WHERE account_id = ? ORDER BY created_at DESC";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);

            ResultSet rs = stmt.executeQuery();
            List<Transaction> transactions = new ArrayList<>();

            while (rs.next()) {
                long relatedAccountId = rs.getLong("related_account_id");
                Long relatedAccountIdOrNull = rs.wasNull() ? null : relatedAccountId;

                transactions.add(new Transaction(
                        rs.getLong("transaction_id"),
                        rs.getLong("account_id"),
                        TransactionType.valueOf(rs.getString("type")),
                        rs.getBigDecimal("amount"),
                        relatedAccountIdOrNull,
                        rs.getTimestamp("created_at").toLocalDateTime()));
            }

            return transactions;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch transactions for account " + accountId, e);
        }
    }
}
