package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.domain.TransactionType;
import com.bankofcli.exception.AccountNotFoundException;
import com.bankofcli.exception.InsufficientFundsException;
import com.bankofcli.exception.InvalidTransactionException;
import com.bankofcli.persistence.AccountDAO;
import com.bankofcli.persistence.ConnectionFactory;
import com.bankofcli.persistence.TransactionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private AccountDAO accountDAO;

    @Mock
    private TransactionDAO transactionDAO;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection connection;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(connectionFactory.getConnection()).thenReturn(connection);
        transactionService = new TransactionServiceImpl(accountDAO, transactionDAO, connectionFactory);
    }

    @Test
    void deposit_validAmount_updatesBalanceAndRecordsTransaction() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", new BigDecimal("0.00")));

        transactionService.deposit(1L, new BigDecimal("50.00"));

        verify(accountDAO).depositFunds(connection, 1L, new BigDecimal("50.00"));
        verify(transactionDAO).createTransaction(connection, 1L, TransactionType.DEPOSIT, new BigDecimal("50.00"), null);
    }

    @Test
    void deposit_negativeAmount_throwsInvalidTransactionException() {
        assertThrows(InvalidTransactionException.class,
                () -> transactionService.deposit(1L, new BigDecimal("-10.00")));
        verify(accountDAO, never()).depositFunds(any(), anyLong(), any());
    }

    @Test
    void withdraw_sufficientFunds_recordsWithdrawal() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", new BigDecimal("100.00")));
        when(accountDAO.withdrawFunds(connection, 1L, new BigDecimal("30.00"))).thenReturn(true);

        transactionService.withdraw(1L, new BigDecimal("30.00"));

        verify(transactionDAO).createTransaction(connection, 1L, TransactionType.WITHDRAWAL, new BigDecimal("30.00"), null);
    }

    @Test
    void withdraw_insufficientFunds_throwsInsufficientFundsException() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", new BigDecimal("10.00")));
        when(accountDAO.withdrawFunds(connection, 1L, new BigDecimal("50.00"))).thenReturn(false);

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.withdraw(1L, new BigDecimal("50.00")));
        verify(transactionDAO, never()).createTransaction(any(), anyLong(), any(), any(), any());
    }

    @Test
    void transfer_validTransfer_movesFundsBetweenAccounts() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", new BigDecimal("100.00")));
        when(accountDAO.getAccountById(2L)).thenReturn(new Account(2L, "4321", new BigDecimal("0.00")));
        when(accountDAO.withdrawFunds(connection, 1L, new BigDecimal("40.00"))).thenReturn(true);

        transactionService.transfer(1L, 2L, new BigDecimal("40.00"));

        verify(accountDAO).depositFunds(connection, 2L, new BigDecimal("40.00"));
        verify(transactionDAO).createTransaction(connection, 1L, TransactionType.TRANSFER, new BigDecimal("-40.00"), 2L);
        verify(transactionDAO).createTransaction(connection, 2L, TransactionType.TRANSFER, new BigDecimal("40.00"), 1L);
    }

    @Test
    void transfer_sameAccount_throwsInvalidTransactionException() {
        assertThrows(InvalidTransactionException.class,
                () -> transactionService.transfer(1L, 1L, new BigDecimal("10.00")));
        verify(accountDAO, never()).withdrawFunds(any(), anyLong(), any());
    }

    @Test
    void transfer_insufficientFunds_throwsInsufficientFundsException() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", new BigDecimal("10.00")));
        when(accountDAO.getAccountById(2L)).thenReturn(new Account(2L, "4321", new BigDecimal("0.00")));
        when(accountDAO.withdrawFunds(connection, 1L, new BigDecimal("50.00"))).thenReturn(false);

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.transfer(1L, 2L, new BigDecimal("50.00")));
        verify(accountDAO, never()).depositFunds(any(), anyLong(), any());
    }

    @Test
    void getHistory_returnsTransactionsFromDao() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", BigDecimal.ZERO));
        List<Transaction> expected = List.of(
                new Transaction(1L, 1L, TransactionType.DEPOSIT, new BigDecimal("10.00"), null, LocalDateTime.now()));
        when(transactionDAO.getTransactionsByAccountId(1L)).thenReturn(expected);

        List<Transaction> history = transactionService.getHistory(1L);

        assertSame(expected, history);
    }

    @Test
    void getHistory_nonExistentAccount_throwsAccountNotFoundException() {
        when(accountDAO.getAccountById(-1L)).thenReturn(null);

        assertThrows(AccountNotFoundException.class, () -> transactionService.getHistory(-1L));
    }
}
