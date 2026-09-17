package com.bankofcli.service;

import com.bankofcli.domain.Account;
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
    void register_validPin() {
        when(accountDAO.createAccount("1234", BigDecimal.ZERO)).thenReturn(new Account(1L, "1234", BigDecimal.ZERO));

        Account account = accountService.register("1234");

        assertEquals(1L, account.getAccountId());
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
    }

    @Test
    void register_invalidPin() {
        assertThrows(InvalidPinException.class, () -> accountService.register("12"));
        verify(accountDAO, never()).createAccount(any(), any());
    }
}
