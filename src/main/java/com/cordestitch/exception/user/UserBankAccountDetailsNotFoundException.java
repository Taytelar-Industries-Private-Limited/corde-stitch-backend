package com.cordestitch.exception.user;

public class UserBankAccountDetailsNotFoundException extends RuntimeException {
    public UserBankAccountDetailsNotFoundException(String message) {
        super(message);
    }
}
