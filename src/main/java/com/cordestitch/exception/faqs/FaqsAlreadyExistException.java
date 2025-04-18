package com.cordestitch.exception.faqs;

public class FaqsAlreadyExistException extends RuntimeException{
    public FaqsAlreadyExistException(String message){
        super(message);
    }
}
