package com.cordestitch.exception.customization;

public class CustomizationNotFoundException extends RuntimeException {
    public CustomizationNotFoundException(String message){
        super(message);
    }
}