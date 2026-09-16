package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.exception.AccountNotEmptyException;
import com.bankofcli.exception.AccountNotFoundException;
import com.bankofcli.exception.InvalidPinException;
import com.bankofcli.persistence.AccountDAO;
import com.bankofcli.persistence.AccountDAOImpl;
import com.bankofcli.persistence.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceImplTest {

    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final AccountService accountService = new AccountServiceImpl(accountDAO);
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
    void register_validPin_createsAccountWithZeroBalance() {
        Account account = accountService.register("1234");
        createdAccountIds.add(account.getAccountId());

        assertTrue(account.getAccountId() > 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
    }

    @Test
    void register_invalidPinFormat_throwsInvalidPinException() {
        assertThrows(InvalidPinException.class, () -> accountService.register("12"));
    }

    @Test
    void login_correctPin_returnsAccount() {
        Account created = accountService.register("4321");
        createdAccountIds.add(created.getAccountId());

        Account loggedIn = accountService.login(created.getAccountId(), "4321");

        assertEquals(created.getAccountId(), loggedIn.getAccountId());
    }

    @Test
    void login_incorrectPin_throwsInvalidPinException() {
        Account created = accountService.register("4321");
        createdAccountIds.add(created.getAccountId());

        assertThrows(InvalidPinException.class, () -> accountService.login(created.getAccountId(), "0000"));
    }

    @Test
    void login_nonExistentAccount_throwsAccountNotFoundException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.login(-1L, "1234"));
    }

    @Test
    void getBalance_existingAccount_returnsBalance() {
        Account created = accountService.register("5555");
        createdAccountIds.add(created.getAccountId());

        BigDecimal balance = accountService.getBalance(created.getAccountId());

        assertEquals(0, BigDecimal.ZERO.compareTo(balance));
    }

    @Test
    void getBalance_nonExistentAccount_throwsAccountNotFoundException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(-1L));
    }

    @Test
    void changePin_correctCurrentPinAndValidNewPin_updatesPin() {
        Account created = accountService.register("1234");
        createdAccountIds.add(created.getAccountId());

        accountService.changePin(created.getAccountId(), "1234", "5678");

        Account loggedIn = accountService.login(created.getAccountId(), "5678");
        assertEquals(created.getAccountId(), loggedIn.getAccountId());
    }

    @Test
    void changePin_incorrectCurrentPin_throwsInvalidPinException() {
        Account created = accountService.register("1234");
        createdAccountIds.add(created.getAccountId());

        assertThrows(InvalidPinException.class, () -> accountService.changePin(created.getAccountId(), "0000", "5678"));
    }

    @Test
    void deleteAccount_zeroBalanceAndCorrectPin_removesAccount() {
        Account created = accountService.register("1234");

        accountService.deleteAccount(created.getAccountId(), "1234");

        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(created.getAccountId()));
    }

    @Test
    void deleteAccount_nonZeroBalance_throwsAccountNotEmptyException() {
        Account created = accountService.register("1234");
        createdAccountIds.add(created.getAccountId());
        accountDAO.depositFunds(created.getAccountId(), new BigDecimal("10.00"));

        assertThrows(AccountNotEmptyException.class, () -> accountService.deleteAccount(created.getAccountId(), "1234"));
    }
}
