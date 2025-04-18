package com.cordestitch.exception.payment;

public class InvalidAccountNumberException extends RuntimeException{
    public InvalidAccountNumberException(String message) {
        super(message);
    }
}
