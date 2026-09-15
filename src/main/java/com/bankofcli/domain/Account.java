package com.bankofcli.domain;

import java.math.BigDecimal;

public class Account {

    private long accountId;
    private String pin;
    private BigDecimal balance;

    public Account(long accountId, String pin, BigDecimal balance) {
        this.accountId = accountId;
        this.pin = pin;
        this.balance = balance;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getPin() {
        return pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
