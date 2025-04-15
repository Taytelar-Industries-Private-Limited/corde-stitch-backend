package com.cordestitch.exception.cart;

public class CartItemNotFoundException extends RuntimeException{

    public CartItemNotFoundException(String message) {
        super(message);
    }
}