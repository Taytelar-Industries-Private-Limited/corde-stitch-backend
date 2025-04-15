package com.cordestitch.exception.user;

public class AccountNumberAlreadyExistsException extends RuntimeException {
    public AccountNumberAlreadyExistsException(String message) {
        super(message);
    }
}