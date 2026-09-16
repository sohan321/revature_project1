package com.bankofcli.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private long transactionId;
    private long accountId;
    private TransactionType type;
    private BigDecimal amount;
    private Long relatedAccountId;
    private LocalDateTime timestamp;

    public Transaction(long transactionId, long accountId, TransactionType type, BigDecimal amount, Long relatedAccountId, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.relatedAccountId = relatedAccountId;
        this.timestamp = timestamp;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
