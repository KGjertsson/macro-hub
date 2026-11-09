package com.kg.macroanalyzer.auth.adaptors.mail;

import com.kg.macroanalyzer.auth.application.port.out.MailPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingMailAdaptor implements MailPort {

    @Override
    public void send(String to, String subject, String body) {
        log.warn("auth.mail.mode=log, so nothing was sent. Mail to {}: {}\n{}", to, subject, body);
    }

}
