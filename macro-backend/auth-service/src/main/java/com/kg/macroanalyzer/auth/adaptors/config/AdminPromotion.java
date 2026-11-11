package com.kg.macroanalyzer.auth.adaptors.config;

import com.kg.macroanalyzer.auth.application.port.out.AuthRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminPromotion {

    private final AuthRepositoryPort repository;
    private final String adminEmail;

    public AdminPromotion(
            AuthRepositoryPort repository, @Value("${auth.admin-email}") String adminEmail) {
        this.repository = repository;
        this.adminEmail = adminEmail.strip().toLowerCase(Locale.ROOT);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void promoteConfiguredAdmin() {
        if (!adminEmail.isEmpty()) {
            repository.promoteToAdmin(adminEmail);
        }
    }

}
