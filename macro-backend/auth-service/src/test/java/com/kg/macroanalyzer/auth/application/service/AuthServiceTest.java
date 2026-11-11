package com.kg.macroanalyzer.auth.application.service;

import com.kg.macroanalyzer.auth.application.port.out.AccessTokenPort;
import com.kg.macroanalyzer.auth.application.port.out.AuthRepositoryPort;
import com.kg.macroanalyzer.auth.application.port.out.MailPort;
import com.kg.macroanalyzer.auth.application.port.out.PasswordHasherPort;
import com.kg.macroanalyzer.auth.domain.InvalidCredentialsException;
import com.kg.macroanalyzer.auth.domain.InvalidSessionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String HASH_OF_NO_PASSWORD = "hash-of-no-password";

    private final AuthRepositoryPort repository = mock(AuthRepositoryPort.class);
    private final PasswordHasherPort hasher = mock(PasswordHasherPort.class);
    private final MailPort mail = mock(MailPort.class);
    private AuthService service;

    @BeforeEach
    void setUp() {
        when(hasher.hash(anyString())).thenReturn(HASH_OF_NO_PASSWORD);
        service = new AuthService(repository, hasher, mock(AccessTokenPort.class), mail,
                Clock.fixed(Instant.parse("2026-11-09T12:00:00Z"), ZoneOffset.UTC),
                Duration.ofDays(30),
                Duration.ofHours(1),
                Duration.ofMinutes(1),
                "http://localhost:8081/",
                "");
    }

    // Skipping the hash would make an unknown address answer much faster, which tells a
    // caller which addresses have accounts
    @Test
    void shouldStillHash_whenTheAddressHasNoAccount() {
        // given
        when(repository.findCredential("anna@example.se")).thenReturn(Optional.empty());

        // when, then
        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login("anna@example.se", "a-long-enough-password"));
        verify(hasher).matches("a-long-enough-password", HASH_OF_NO_PASSWORD);
    }

    @Test
    void shouldNotMail_whenTheThrottleRefusesTheToken() {
        // given
        when(repository.userExists("anna@example.se")).thenReturn(false);
        when(repository.saveEmailTokenUnlessIssuedSince(
                any(), eq(Instant.parse("2026-11-09T11:59:00Z")))).thenReturn(false);

        // when
        service.requestRegistration("anna@example.se");

        // then
        verify(mail, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldRejectARefresh_withoutACookie() {
        assertThrows(InvalidSessionException.class, () -> service.refresh(null));
    }

}
