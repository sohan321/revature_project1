package com.bankofcli.persistence;

import com.bankofcli.domain.Account;

import java.math.BigDecimal;
import java.sql.Connection;

public interface AccountDAO {

    Account createAccount(String pin, BigDecimal balance);

    Account getAccountById(long accountId);

    void depositFunds(long accountId, BigDecimal amount);

    void depositFunds(Connection connection, long accountId, BigDecimal amount);

    boolean withdrawFunds(long accountId, BigDecimal amount);

    boolean withdrawFunds(Connection connection, long accountId, BigDecimal amount);
}
