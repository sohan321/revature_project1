package com.bankofcli.exception;

public abstract class BankingException extends RuntimeException {

    protected BankingException(String message) {
        super(message);
    }
}
