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
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM accounts WHERE account_id = ?")) {
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
    void updateBalance_existingAccount_persistsNewBalance() {
        Account created = accountDAO.createAccount("5555", new BigDecimal("0.00"));
        createdAccountIds.add(created.getAccountId());

        accountDAO.updateBalance(created.getAccountId(), new BigDecimal("99.99"));

        Account updated = accountDAO.getAccountById(created.getAccountId());
        assertNotNull(updated);
        assertEquals(0, new BigDecimal("99.99").compareTo(updated.getBalance()));
    }

    @Test
    void updateBalance_nonExistentAccount_doesNotThrowOrCreateRow() {
        assertDoesNotThrow(() -> accountDAO.updateBalance(-1L, new BigDecimal("10.00")));

        assertNull(accountDAO.getAccountById(-1L));
    }
}
