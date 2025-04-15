package com.cordestitch.exception.user;

public class DataCheckReflectionException extends RuntimeException {
    public DataCheckReflectionException(String message) {
        super(message);
    }

    public DataCheckReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
