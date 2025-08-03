package com.eaglebank.exception;

public class BankAccountNotFoundException extends RuntimeException {

    public BankAccountNotFoundException(String message) {
        super(message);
    }

    public BankAccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
