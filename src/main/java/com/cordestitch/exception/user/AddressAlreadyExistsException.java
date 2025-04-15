package com.cordestitch.exception.user;

public class AddressAlreadyExistsException extends RuntimeException {
    public AddressAlreadyExistsException(String message) {
        super(message);
    }
}