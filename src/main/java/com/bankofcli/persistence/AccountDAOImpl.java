package com.bankofcli.persistence;

import com.bankofcli.domain.Account;
import com.bankofcli.exception.DataAccessException;
import com.bankofcli.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public Account createAccount(String pin, BigDecimal balance) {
        String sql = "INSERT INTO accounts (pin, balance) VALUES (?, ?) RETURNING account_id";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pin);
            stmt.setBigDecimal(2, balance);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            return new Account(rs.getLong("account_id"), pin, balance);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create account", e);
        }
    }

    @Override
    public Account getAccountById(long accountId) {
        String sql = "SELECT account_id, pin, balance FROM accounts WHERE account_id = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            return new Account(
                    rs.getLong("account_id"),
                    rs.getString("pin"),
                    rs.getBigDecimal("balance"));

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch account " + accountId, e);
        }
    }

    @Override
    public void updateBalance(long accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, newBalance);
            stmt.setLong(2, accountId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update balance for account " + accountId, e);
        }
    }
}
