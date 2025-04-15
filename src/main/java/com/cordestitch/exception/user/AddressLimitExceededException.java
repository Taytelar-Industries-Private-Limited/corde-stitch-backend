package com.cordestitch.exception.user;

public class AddressLimitExceededException extends RuntimeException {
    public AddressLimitExceededException(String message) {
        super(message);
    }
}