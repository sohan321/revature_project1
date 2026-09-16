package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.domain.TransactionType;
import com.bankofcli.exception.AccountNotFoundException;
import com.bankofcli.exception.BankingException;
import com.bankofcli.exception.DataAccessException;
import com.bankofcli.exception.InsufficientFundsException;
import com.bankofcli.exception.InvalidTransactionException;
import com.bankofcli.persistence.AccountDAO;
import com.bankofcli.persistence.ConnectionFactory;
import com.bankofcli.persistence.TransactionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final ConnectionFactory connectionFactory;

    public TransactionServiceImpl(AccountDAO accountDAO, TransactionDAO transactionDAO, ConnectionFactory connectionFactory) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void deposit(long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        findAccountOrThrow(accountId);

        executeInTransaction(new Consumer<Connection>() {
            @Override
            public void accept(Connection connection) {
                accountDAO.depositFunds(connection, accountId, amount);
                transactionDAO.createTransaction(connection, accountId, TransactionType.DEPOSIT, amount, null);
            }
        });

        logger.info("Deposited {} into account {}", amount, accountId);
    }

    @Override
    public void withdraw(long accountId, BigDecimal amount) {
        validatePositiveAmount(amount);
        findAccountOrThrow(accountId);

        executeInTransaction(new Consumer<Connection>() {
            @Override
            public void accept(Connection connection) {
                boolean withdrawn = accountDAO.withdrawFunds(connection, accountId, amount);
                if (!withdrawn) {
                    logger.error("Withdrawal failed: insufficient funds for account {}", accountId);
                    throw new InsufficientFundsException("Insufficient funds in account " + accountId);
                }
                transactionDAO.createTransaction(connection, accountId, TransactionType.WITHDRAWAL, amount, null);
            }
        });

        logger.info("Withdrew {} from account {}", amount, accountId);
    }

    @Override
    public void transfer(long fromAccountId, long toAccountId, BigDecimal amount) {
        validatePositiveAmount(amount);

        if (fromAccountId == toAccountId) {
            logger.error("Transfer failed: cannot transfer to the same account {}", fromAccountId);
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }

        findAccountOrThrow(fromAccountId);
        findAccountOrThrow(toAccountId);

        executeInTransaction(new Consumer<Connection>() {
            @Override
            public void accept(Connection connection) {
                boolean withdrawn = accountDAO.withdrawFunds(connection, fromAccountId, amount);
                if (!withdrawn) {
                    logger.error("Transfer failed: insufficient funds for account {}", fromAccountId);
                    throw new InsufficientFundsException("Insufficient funds in account " + fromAccountId);
                }
                accountDAO.depositFunds(connection, toAccountId, amount);
                transactionDAO.createTransaction(connection, fromAccountId, TransactionType.TRANSFER, amount.negate(), toAccountId);
                transactionDAO.createTransaction(connection, toAccountId, TransactionType.TRANSFER, amount, fromAccountId);
            }
        });

        logger.info("Transferred {} from account {} to account {}", amount, fromAccountId, toAccountId);
    }

    @Override
    public List<Transaction> getHistory(long accountId) {
        findAccountOrThrow(accountId);
        return transactionDAO.getTransactionsByAccountId(accountId);
    }

    private Account findAccountOrThrow(long accountId) {
        Account account = accountDAO.getAccountById(accountId);

        if (account == null) {
            logger.error("No account found with ID {}", accountId);
            throw new AccountNotFoundException("No account found with ID " + accountId);
        }

        return account;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Transaction failed: amount must be positive");
            throw new InvalidTransactionException("Amount must be positive");
        }
    }

    private void executeInTransaction(Consumer<Connection> operation) {
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                connection.setAutoCommit(false);
                operation.accept(connection);
                connection.commit();
            } catch (BankingException e) {
                connection.rollback();
                throw e;
            } catch (RuntimeException | SQLException e) {
                connection.rollback();
                throw new DataAccessException("Transaction failed", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not complete transaction", e);
        }
    }
}
