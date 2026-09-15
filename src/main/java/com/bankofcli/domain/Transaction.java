package com.bankofcli.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private long transactionId;
    private long accountId;
    private TransactionType type;
    private BigDecimal amount;
    private Long relatedAccountId;
    private LocalDateTime createdAt;

    public Transaction(long transactionId, long accountId, TransactionType type, BigDecimal amount, Long relatedAccountId, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.relatedAccountId = relatedAccountId;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
