package com.cordestitch.emailhandler;

import jakarta.mail.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class DefaultEmailTransport implements EmailTransport{

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.username}")
    private String emailUsername;

    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${spring.mail.port}")
    private int mailPort;


    @Override
    public void send(Message message) throws MessagingException {

        Properties properties = new Properties();
        properties.put("mail.smtp.host", mailHost);
        properties.put("mail.smtp.port", mailPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });

        Transport.send(message);
    }
}
