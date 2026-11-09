package com.kg.macroanalyzer.scrape.adaptors.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScrapeSecurityConfigurationTest {

    private static final String ISSUER = "macro-hub-auth";
    private static final String AUDIENCE = "macro-hub";

    @Test
    void shouldAcceptATokenFromAuth_forThisAudience() {
        assertFalse(hasErrors(token(ISSUER, List.of(AUDIENCE), Duration.ofMinutes(10))));
    }

    @Test
    void shouldRejectAnotherIssuer() {
        assertTrue(hasErrors(token("someone-else", List.of(AUDIENCE), Duration.ofMinutes(10))));
    }

    @Test
    void shouldRejectAnotherAudience() {
        assertTrue(hasErrors(token(ISSUER, List.of("another-app"), Duration.ofMinutes(10))));
    }

    @Test
    void shouldRejectAnExpiredToken() {
        assertTrue(hasErrors(token(ISSUER, List.of(AUDIENCE), Duration.ofMinutes(-10))));
    }

    private static boolean hasErrors(Jwt token) {
        return ScrapeSecurityConfiguration.accessTokenValidator().validate(token).hasErrors();
    }

    private static Jwt token(String issuer, List<String> audience, Duration expiresIn) {
        final var now = Instant.now();

        return Jwt.withTokenValue("token")
                .header("alg", "ES256")
                .issuer(issuer)
                .audience(audience)
                .issuedAt(now.minus(Duration.ofHours(1)))
                .expiresAt(now.plus(expiresIn))
                .build();
    }

}
