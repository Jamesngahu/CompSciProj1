package com.cbcportal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendLoginCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your CBC Portal login code");
        message.setText("Your login verification code is: " + code
                + "\n\nThis code expires in 5 minutes. If you did not request it, you can ignore this email.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            // No SMTP server configured (or it failed) - fall back to logging the
            // code so the app remains usable without real email credentials.
            log.warn("Could not email login code to {} ({}). Code: {}", toEmail, e.getMessage(), code);
        }
    }
}
