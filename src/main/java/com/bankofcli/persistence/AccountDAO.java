package com.bankofcli.persistence;

import com.bankofcli.domain.Account;

import java.math.BigDecimal;

public interface AccountDAO {

    Account createAccount(String pin, BigDecimal balance);

    Account getAccountById(long accountId);

    void updateBalance(long accountId, BigDecimal newBalance);
}
