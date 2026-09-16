package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.exception.AccountNotEmptyException;
import com.bankofcli.exception.AccountNotFoundException;
import com.bankofcli.exception.InvalidPinException;
import com.bankofcli.persistence.AccountDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountDAO accountDAO;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountDAO);
    }

    @Test
    void register_validPin_createsAccountWithZeroBalance() {
        when(accountDAO.createAccount("1234", BigDecimal.ZERO)).thenReturn(new Account(1L, "1234", BigDecimal.ZERO));

        Account account = accountService.register("1234");

        assertEquals(1L, account.getAccountId());
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
    }

    @Test
    void register_invalidPinFormat_throwsInvalidPinException() {
        assertThrows(InvalidPinException.class, () -> accountService.register("12"));
        verify(accountDAO, never()).createAccount(any(), any());
    }

    @Test
    void login_correctPin_returnsAccount() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "4321", new BigDecimal("10.00")));

        Account loggedIn = accountService.login(1L, "4321");

        assertEquals(1L, loggedIn.getAccountId());
    }

    @Test
    void login_incorrectPin_throwsInvalidPinException() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "4321", BigDecimal.ZERO));

        assertThrows(InvalidPinException.class, () -> accountService.login(1L, "0000"));
    }

    @Test
    void login_nonExistentAccount_throwsAccountNotFoundException() {
        when(accountDAO.getAccountById(-1L)).thenReturn(null);

        assertThrows(AccountNotFoundException.class, () -> accountService.login(-1L, "1234"));
    }

    @Test
    void getBalance_existingAccount_returnsBalance() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "5555", new BigDecimal("50.00")));

        BigDecimal balance = accountService.getBalance(1L);

        assertEquals(0, new BigDecimal("50.00").compareTo(balance));
    }

    @Test
    void getBalance_nonExistentAccount_throwsAccountNotFoundException() {
        when(accountDAO.getAccountById(-1L)).thenReturn(null);

        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(-1L));
    }

    @Test
    void changePin_correctCurrentPinAndValidNewPin_updatesPin() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", BigDecimal.ZERO));

        accountService.changePin(1L, "1234", "5678");

        verify(accountDAO).updatePin(1L, "5678");
    }

    @Test
    void changePin_incorrectCurrentPin_throwsInvalidPinException() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", BigDecimal.ZERO));

        assertThrows(InvalidPinException.class, () -> accountService.changePin(1L, "0000", "5678"));
        verify(accountDAO, never()).updatePin(eq(1L), any());
    }

    @Test
    void deleteAccount_zeroBalanceAndCorrectPin_removesAccount() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", BigDecimal.ZERO));

        accountService.deleteAccount(1L, "1234");

        verify(accountDAO).deleteAccount(1L);
    }

    @Test
    void deleteAccount_nonZeroBalance_throwsAccountNotEmptyException() {
        when(accountDAO.getAccountById(1L)).thenReturn(new Account(1L, "1234", new BigDecimal("10.00")));

        assertThrows(AccountNotEmptyException.class, () -> accountService.deleteAccount(1L, "1234"));
        verify(accountDAO, never()).deleteAccount(1L);
    }
}
