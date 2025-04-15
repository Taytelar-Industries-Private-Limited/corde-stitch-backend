package com.cordestitch.exception.order;

public class OrderCancellationException extends RuntimeException {
    public OrderCancellationException(String message){
        super(message);
    }
}
