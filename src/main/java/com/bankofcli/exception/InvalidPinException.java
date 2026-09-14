package com.bankofcli.exception;

public class InvalidPinException extends BankingException {

    public InvalidPinException(String message) {
        super(message);
    }
}
