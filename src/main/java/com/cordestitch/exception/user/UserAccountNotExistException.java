package com.cordestitch.exception.user;

public class UserAccountNotExistException extends RuntimeException{

    public UserAccountNotExistException(String message) {
        super(message);
    }
}
