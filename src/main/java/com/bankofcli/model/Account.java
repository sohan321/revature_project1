package com.bankofcli.model;

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

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
