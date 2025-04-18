package com.cordestitch.exception.user;

public class UserAccountAlreadyExistException extends RuntimeException{
    public UserAccountAlreadyExistException(String message){
        super(message);
    }
}
