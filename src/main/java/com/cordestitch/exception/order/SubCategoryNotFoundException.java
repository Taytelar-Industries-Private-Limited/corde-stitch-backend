package com.cordestitch.exception.order;

public class SubCategoryNotFoundException extends RuntimeException{
    public SubCategoryNotFoundException(String message) {
        super(message);
    }
}
