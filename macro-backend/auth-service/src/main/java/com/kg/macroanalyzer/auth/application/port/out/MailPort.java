package com.kg.macroanalyzer.auth.application.port.out;

public interface MailPort {

    void send(String to, String subject, String body);

}
