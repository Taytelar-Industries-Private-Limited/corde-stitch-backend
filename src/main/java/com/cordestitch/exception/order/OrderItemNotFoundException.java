package com.cordestitch.exception.order;

public class OrderItemNotFoundException extends RuntimeException{

    public OrderItemNotFoundException(String message) {
        super(message);
    }
}
