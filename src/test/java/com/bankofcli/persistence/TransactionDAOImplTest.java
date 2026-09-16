package com.bankofcli.persistence;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.domain.TransactionType;
import com.bankofcli.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionDAOImplTest {

    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = accountDAO.createAccount("9999", new BigDecimal("100.00"));
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM transaction WHERE account_id = ?")) {
                stmt.setLong(1, testAccount.getAccountId());
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM account WHERE account_id = ?")) {
                stmt.setLong(1, testAccount.getAccountId());
                stmt.executeUpdate();
            }
        }
    }

    @Test
    void createTransaction_deposit_savesAndReturnsGeneratedFields() {
        Transaction txn = transactionDAO.createTransaction(
                testAccount.getAccountId(), TransactionType.DEPOSIT, new BigDecimal("25.00"), null);

        assertTrue(txn.getTransactionId() > 0);
        assertNotNull(txn.getTimestamp());
        assertNull(txn.getRelatedAccountId());
        assertEquals(TransactionType.DEPOSIT, txn.getType());
    }

    @Test
    void createTransaction_invalidAccountId_throwsDataAccessException() {
        assertThrows(DataAccessException.class,
                () -> transactionDAO.createTransaction(-1L, TransactionType.DEPOSIT, new BigDecimal("10.00"), null));
    }

    @Test
    void createTransaction_withConnection_deposit_savesAndReturnsGeneratedFields() throws SQLException {
        Transaction txn;
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            txn = transactionDAO.createTransaction(
                    conn, testAccount.getAccountId(), TransactionType.DEPOSIT, new BigDecimal("25.00"), null);
        }

        assertTrue(txn.getTransactionId() > 0);
        assertNotNull(txn.getTimestamp());
        assertEquals(TransactionType.DEPOSIT, txn.getType());
    }

    @Test
    void createTransaction_withConnection_invalidAccountId_throwsDataAccessException() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            assertThrows(DataAccessException.class, () -> transactionDAO.createTransaction(
                    conn, -1L, TransactionType.DEPOSIT, new BigDecimal("10.00"), null));
        }
    }

    @Test
    void getTransactionsByAccountId_returnsCreatedTransactions() {
        transactionDAO.createTransaction(testAccount.getAccountId(), TransactionType.DEPOSIT, new BigDecimal("25.00"), null);
        transactionDAO.createTransaction(testAccount.getAccountId(), TransactionType.WITHDRAWAL, new BigDecimal("5.00"), null);

        List<Transaction> history = transactionDAO.getTransactionsByAccountId(testAccount.getAccountId());

        assertEquals(2, history.size());
    }

    @Test
    void getTransactionsByAccountId_noTransactions_returnsEmptyList() {
        List<Transaction> history = transactionDAO.getTransactionsByAccountId(testAccount.getAccountId());

        assertTrue(history.isEmpty());
    }
}
