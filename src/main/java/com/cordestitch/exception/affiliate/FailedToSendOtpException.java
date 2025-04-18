package com.cordestitch.exception.affiliate;

public class FailedToSendOtpException extends RuntimeException{
    public FailedToSendOtpException(String message) {
        super(message);
    }
}
