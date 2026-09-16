package com.bankofcli.service;

import com.bankofcli.domain.Account;

import java.math.BigDecimal;

public interface AccountService {

    Account register(String pin);

    Account login(long accountId, String pin);

    BigDecimal getBalance(long accountId);

    void changePin(long accountId, String currentPin, String newPin);

    void deleteAccount(long accountId, String pin);
}
