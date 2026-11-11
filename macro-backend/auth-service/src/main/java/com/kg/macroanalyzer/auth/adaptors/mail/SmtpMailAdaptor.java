package com.kg.macroanalyzer.auth.adaptors.mail;

import com.kg.macroanalyzer.auth.application.port.out.MailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
public class SmtpMailAdaptor implements MailPort {

    private final JavaMailSender sender;
    private final String from;

    public SmtpMailAdaptor(JavaMailSender sender, String from) {
        this.sender = sender;
        this.from = from;
    }

    // On its own thread, so the response time doesn't tell whether a mail was sent, which would
    // show which addresses have accounts
    @Override
    public void send(String to, String subject, String body) {
        final var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        Thread.startVirtualThread(() -> {
            try {
                sender.send(message);
            } catch (MailException e) {
                log.error("Could not send a mail", e);
            }
        });
    }

}
