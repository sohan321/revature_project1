package com.bankofcli.exception;

public class InvalidTransactionException extends BankingException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
