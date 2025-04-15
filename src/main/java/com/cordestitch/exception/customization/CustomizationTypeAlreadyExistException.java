package com.cordestitch.exception.customization;

public class CustomizationTypeAlreadyExistException extends RuntimeException{
    public CustomizationTypeAlreadyExistException(String message){
        super(message);
    }
}
