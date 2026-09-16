package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.exception.AccountNotFoundException;
import com.bankofcli.exception.InvalidPinException;
import com.bankofcli.persistence.AccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountDAO accountDAO;

    public AccountServiceImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public Account register(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            logger.error("Registration failed: PIN must be exactly 4 digits");
            throw new InvalidPinException("PIN must be exactly 4 digits");
        }

        Account account = accountDAO.createAccount(pin, BigDecimal.ZERO);
        logger.info("Account {} registered successfully", account.getAccountId());
        return account;
    }

    @Override
    public Account login(long accountId, String pin) {
        Account account = findAccountOrThrow(accountId);

        if (!account.getPin().equals(pin)) {
            logger.error("Login failed: incorrect PIN for account {}", accountId);
            throw new InvalidPinException("Incorrect PIN");
        }

        logger.info("Account {} logged in successfully", accountId);
        return account;
    }

    @Override
    public BigDecimal getBalance(long accountId) {
        BigDecimal balance = findAccountOrThrow(accountId).getBalance();
        logger.info("Balance retrieved for account {}", accountId);
        return balance;
    }

    private Account findAccountOrThrow(long accountId) {
        Account account = accountDAO.getAccountById(accountId);

        if (account == null) {
            logger.error("No account found with ID {}", accountId);
            throw new AccountNotFoundException("No account found with ID " + accountId);
        }

        return account;
    }
}
