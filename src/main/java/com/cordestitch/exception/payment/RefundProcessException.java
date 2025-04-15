package com.cordestitch.exception.payment;

public class RefundProcessException extends RuntimeException{
    public RefundProcessException(String message) {
        super(message);
    }
    public RefundProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
