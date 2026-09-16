package com.bankofcli.persistence;

import com.bankofcli.domain.Account;
import com.bankofcli.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountDAOImplTest {

    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final List<Long> createdAccountIds = new ArrayList<>();

    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM account WHERE account_id = ?")) {
            for (Long id : createdAccountIds) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
        }
        createdAccountIds.clear();
    }

    @Test
    void createAccount_savesAndReturnsAccountWithGeneratedId() {
        Account account = accountDAO.createAccount("1234", new BigDecimal("50.00"));
        createdAccountIds.add(account.getAccountId());

        assertTrue(account.getAccountId() > 0);
        assertEquals("1234", account.getPin());
        assertEquals(0, new BigDecimal("50.00").compareTo(account.getBalance()));
    }

    @Test
    void createAccount_nullPin_throwsDataAccessException() {
        assertThrows(DataAccessException.class,
                () -> accountDAO.createAccount(null, new BigDecimal("50.00")));
    }

    @Test
    void getAccountById_existingAccount_returnsAccount() {
        Account created = accountDAO.createAccount("4321", new BigDecimal("10.00"));
        createdAccountIds.add(created.getAccountId());

        Account found = accountDAO.getAccountById(created.getAccountId());

        assertNotNull(found);
        assertEquals(created.getAccountId(), found.getAccountId());
    }

    @Test
    void getAccountById_nonExistentAccount_returnsNull() {
        Account found = accountDAO.getAccountById(-1L);

        assertNull(found);
    }

    @Test
    void depositFunds_existingAccount_increasesBalance() {
        Account created = accountDAO.createAccount("5555", new BigDecimal("20.00"));
        createdAccountIds.add(created.getAccountId());

        accountDAO.depositFunds(created.getAccountId(), new BigDecimal("30.00"));

        Account updated = accountDAO.getAccountById(created.getAccountId());
        assertNotNull(updated);
        assertEquals(0, new BigDecimal("50.00").compareTo(updated.getBalance()));
    }

    @Test
    void depositFunds_nonExistentAccount_doesNotThrowOrCreateRow() {
        assertDoesNotThrow(() -> accountDAO.depositFunds(-1L, new BigDecimal("10.00")));

        assertNull(accountDAO.getAccountById(-1L));
    }

    @Test
    void withdrawFunds_sufficientFunds_decreasesBalanceAndReturnsTrue() {
        Account created = accountDAO.createAccount("6666", new BigDecimal("100.00"));
        createdAccountIds.add(created.getAccountId());

        boolean withdrawn = accountDAO.withdrawFunds(created.getAccountId(), new BigDecimal("40.00"));

        assertTrue(withdrawn);
        Account updated = accountDAO.getAccountById(created.getAccountId());
        assertEquals(0, new BigDecimal("60.00").compareTo(updated.getBalance()));
    }

    @Test
    void withdrawFunds_insufficientFunds_returnsFalseAndLeavesBalanceUnchanged() {
        Account created = accountDAO.createAccount("7777", new BigDecimal("10.00"));
        createdAccountIds.add(created.getAccountId());

        boolean withdrawn = accountDAO.withdrawFunds(created.getAccountId(), new BigDecimal("50.00"));

        assertFalse(withdrawn);
        Account unchanged = accountDAO.getAccountById(created.getAccountId());
        assertEquals(0, new BigDecimal("10.00").compareTo(unchanged.getBalance()));
    }

    @Test
    void depositFunds_withConnection_existingAccount_increasesBalance() throws SQLException {
        Account created = accountDAO.createAccount("8888", new BigDecimal("20.00"));
        createdAccountIds.add(created.getAccountId());

        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            accountDAO.depositFunds(conn, created.getAccountId(), new BigDecimal("30.00"));
        }

        Account updated = accountDAO.getAccountById(created.getAccountId());
        assertEquals(0, new BigDecimal("50.00").compareTo(updated.getBalance()));
    }

    @Test
    void depositFunds_withConnection_nonExistentAccount_doesNotThrowOrCreateRow() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            assertDoesNotThrow(() -> accountDAO.depositFunds(conn, -1L, new BigDecimal("10.00")));
        }

        assertNull(accountDAO.getAccountById(-1L));
    }

    @Test
    void withdrawFunds_withConnection_sufficientFunds_decreasesBalanceAndReturnsTrue() throws SQLException {
        Account created = accountDAO.createAccount("8889", new BigDecimal("100.00"));
        createdAccountIds.add(created.getAccountId());

        boolean withdrawn;
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            withdrawn = accountDAO.withdrawFunds(conn, created.getAccountId(), new BigDecimal("40.00"));
        }

        assertTrue(withdrawn);
        Account updated = accountDAO.getAccountById(created.getAccountId());
        assertEquals(0, new BigDecimal("60.00").compareTo(updated.getBalance()));
    }

    @Test
    void withdrawFunds_withConnection_insufficientFunds_returnsFalseAndLeavesBalanceUnchanged() throws SQLException {
        Account created = accountDAO.createAccount("8890", new BigDecimal("10.00"));
        createdAccountIds.add(created.getAccountId());

        boolean withdrawn;
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            withdrawn = accountDAO.withdrawFunds(conn, created.getAccountId(), new BigDecimal("50.00"));
        }

        assertFalse(withdrawn);
        Account unchanged = accountDAO.getAccountById(created.getAccountId());
        assertEquals(0, new BigDecimal("10.00").compareTo(unchanged.getBalance()));
    }

    @Test
    void updatePin_existingAccount_changesPin() {
        Account created = accountDAO.createAccount("1111", new BigDecimal("0.00"));
        createdAccountIds.add(created.getAccountId());

        accountDAO.updatePin(created.getAccountId(), "9999");

        Account updated = accountDAO.getAccountById(created.getAccountId());
        assertEquals("9999", updated.getPin());
    }

    @Test
    void updatePin_nonExistentAccount_doesNotThrow() {
        assertDoesNotThrow(() -> accountDAO.updatePin(-1L, "9999"));
    }

    @Test
    void deleteAccount_existingAccount_removesAccount() {
        Account created = accountDAO.createAccount("2222", new BigDecimal("0.00"));

        accountDAO.deleteAccount(created.getAccountId());

        assertNull(accountDAO.getAccountById(created.getAccountId()));
    }

    @Test
    void deleteAccount_nonExistentAccount_doesNotThrow() {
        assertDoesNotThrow(() -> accountDAO.deleteAccount(-1L));
    }
}
