package com.cordestitch.exception.customization;

public class FabricNotFoundException extends RuntimeException {
    public FabricNotFoundException(String message) {
        super(message);
    }
}