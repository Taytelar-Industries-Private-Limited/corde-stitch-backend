package com.cordestitch.exception.payment;

public class PaymentAlreadyConfirmedException extends RuntimeException {
    public PaymentAlreadyConfirmedException(String message) {
        super(message);
    }
}
