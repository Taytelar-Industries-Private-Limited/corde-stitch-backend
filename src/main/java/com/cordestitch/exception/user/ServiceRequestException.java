package com.cordestitch.exception.user;

public class ServiceRequestException extends RuntimeException{
    public ServiceRequestException(String message) {
        super(message);
    }
}
