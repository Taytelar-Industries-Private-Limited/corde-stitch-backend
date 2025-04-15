package com.cordestitch.exception.alteration;

public class SlotAlreadyBookedException extends RuntimeException{
    public SlotAlreadyBookedException(String message) {
        super(message);
    }
}
