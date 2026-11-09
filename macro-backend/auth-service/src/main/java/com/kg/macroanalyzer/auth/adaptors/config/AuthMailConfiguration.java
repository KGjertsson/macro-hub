package com.kg.macroanalyzer.auth.adaptors.config;

import com.kg.macroanalyzer.auth.adaptors.mail.LoggingMailAdaptor;
import com.kg.macroanalyzer.auth.adaptors.mail.SmtpMailAdaptor;
import com.kg.macroanalyzer.auth.application.port.out.MailPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class AuthMailConfiguration {

    @Bean
    @ConditionalOnProperty(name = "auth.mail.mode", havingValue = "smtp")
    MailPort smtpMailPort(JavaMailSender sender, @Value("${auth.mail.from}") String from) {
        return new SmtpMailAdaptor(sender, from);
    }

    @Bean
    @ConditionalOnProperty(name = "auth.mail.mode", havingValue = "log")
    MailPort loggingMailPort() {
        return new LoggingMailAdaptor();
    }

}
