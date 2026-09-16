package com.bankofcli.service;

import com.bankofcli.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    void deposit(long accountId, BigDecimal amount);

    void withdraw(long accountId, BigDecimal amount);

    void transfer(long fromAccountId, long toAccountId, BigDecimal amount);

    List<Transaction> getHistory(long accountId);
}
