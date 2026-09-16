package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.exception.AccountNotFoundException;
import com.bankofcli.exception.InsufficientFundsException;
import com.bankofcli.exception.InvalidTransactionException;
import com.bankofcli.persistence.AccountDAO;
import com.bankofcli.persistence.AccountDAOImpl;
import com.bankofcli.persistence.ConnectionFactory;
import com.bankofcli.persistence.TransactionDAO;
import com.bankofcli.persistence.TransactionDAOImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceImplTest {

    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();
    private final AccountService accountService = new AccountServiceImpl(accountDAO);
    private final TransactionService transactionService = new TransactionServiceImpl(accountDAO, transactionDAO);
    private final List<Long> createdAccountIds = new ArrayList<>();

    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = ConnectionFactory.getConnectionFactory().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM transaction WHERE account_id = ANY(?) OR related_account_id = ANY(?)")) {
                Long[] ids = createdAccountIds.toArray(new Long[0]);
                stmt.setArray(1, conn.createArrayOf("INTEGER", ids));
                stmt.setArray(2, conn.createArrayOf("INTEGER", ids));
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM account WHERE account_id = ?")) {
                for (Long id : createdAccountIds) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
            }
        }
        createdAccountIds.clear();
    }

    private Account openAccount(String pin, String startingBalance) {
        Account account = accountService.register(pin);
        createdAccountIds.add(account.getAccountId());
        if (new BigDecimal(startingBalance).compareTo(BigDecimal.ZERO) > 0) {
            transactionService.deposit(account.getAccountId(), new BigDecimal(startingBalance));
        }
        return accountDAO.getAccountById(account.getAccountId());
    }

    @Test
    void deposit_validAmount_increasesBalance() {
        Account account = openAccount("1111", "0.00");

        transactionService.deposit(account.getAccountId(), new BigDecimal("50.00"));

        BigDecimal balance = accountService.getBalance(account.getAccountId());
        assertEquals(0, new BigDecimal("50.00").compareTo(balance));
    }

    @Test
    void deposit_negativeAmount_throwsInvalidTransactionException() {
        Account account = openAccount("1111", "0.00");

        assertThrows(InvalidTransactionException.class,
                () -> transactionService.deposit(account.getAccountId(), new BigDecimal("-10.00")));
    }

    @Test
    void withdraw_sufficientFunds_decreasesBalance() {
        Account account = openAccount("2222", "100.00");

        transactionService.withdraw(account.getAccountId(), new BigDecimal("30.00"));

        BigDecimal balance = accountService.getBalance(account.getAccountId());
        assertEquals(0, new BigDecimal("70.00").compareTo(balance));
    }

    @Test
    void withdraw_insufficientFunds_throwsInsufficientFundsException() {
        Account account = openAccount("2222", "10.00");

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.withdraw(account.getAccountId(), new BigDecimal("50.00")));
    }

    @Test
    void transfer_validTransfer_movesBalanceBetweenAccounts() {
        Account from = openAccount("3333", "100.00");
        Account to = openAccount("4444", "0.00");

        transactionService.transfer(from.getAccountId(), to.getAccountId(), new BigDecimal("40.00"));

        assertEquals(0, new BigDecimal("60.00").compareTo(accountService.getBalance(from.getAccountId())));
        assertEquals(0, new BigDecimal("40.00").compareTo(accountService.getBalance(to.getAccountId())));
    }

    @Test
    void transfer_sameAccount_throwsInvalidTransactionException() {
        Account account = openAccount("3333", "100.00");

        assertThrows(InvalidTransactionException.class,
                () -> transactionService.transfer(account.getAccountId(), account.getAccountId(), new BigDecimal("10.00")));
    }

    @Test
    void transfer_insufficientFunds_throwsInsufficientFundsException() {
        Account from = openAccount("3333", "10.00");
        Account to = openAccount("4444", "0.00");

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.transfer(from.getAccountId(), to.getAccountId(), new BigDecimal("50.00")));
    }

    @Test
    void getHistory_returnsRecordedTransactions() {
        Account account = openAccount("5555", "0.00");
        transactionService.deposit(account.getAccountId(), new BigDecimal("20.00"));
        transactionService.withdraw(account.getAccountId(), new BigDecimal("5.00"));

        List<Transaction> history = transactionService.getHistory(account.getAccountId());

        assertEquals(2, history.size());
    }

    @Test
    void getHistory_nonExistentAccount_throwsAccountNotFoundException() {
        assertThrows(AccountNotFoundException.class, () -> transactionService.getHistory(-1L));
    }
}
