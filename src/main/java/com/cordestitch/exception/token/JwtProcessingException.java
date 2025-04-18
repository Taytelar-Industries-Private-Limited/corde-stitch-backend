package com.cordestitch.exception.token;

public class JwtProcessingException extends RuntimeException{

    public JwtProcessingException(String message) {
        super(message);
    }
}
