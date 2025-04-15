package com.cordestitch.emailhandler;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

public interface EmailTransport {
    void send(Message message) throws MessagingException;

}
