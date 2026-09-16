package com.bankofcli.exception;

public class AccountNotEmptyException extends BankingException {

    public AccountNotEmptyException(String message) {
        super(message);
    }
}
