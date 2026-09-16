package com.bankofcli.persistence;

import com.bankofcli.domain.Transaction;
import com.bankofcli.domain.TransactionType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public interface TransactionDAO {

    Transaction createTransaction(long accountId, TransactionType type, BigDecimal amount, Long relatedAccountId);

    Transaction createTransaction(Connection connection, long accountId, TransactionType type, BigDecimal amount, Long relatedAccountId);

    List<Transaction> getTransactionsByAccountId(long accountId);
}
