package com.kg.macroanalyzer.auth.application.port.out;

import com.kg.macroanalyzer.auth.domain.EmailTokenPurpose;

import java.time.Instant;

public record NewEmailToken(String tokenHash, String email, EmailTokenPurpose purpose, Instant created, Instant expiresAt) {
}
