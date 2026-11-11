package com.kg.macroanalyzer.auth.application.service;

import com.kg.macroanalyzer.auth.application.port.in.AuthCommands;
import com.kg.macroanalyzer.auth.application.port.in.Authenticated;
import com.kg.macroanalyzer.auth.application.port.in.Login;
import com.kg.macroanalyzer.auth.application.port.out.AccessTokenPort;
import com.kg.macroanalyzer.auth.application.port.out.AuthRepositoryPort;
import com.kg.macroanalyzer.auth.application.port.out.MailPort;
import com.kg.macroanalyzer.auth.application.port.out.NewAccount;
import com.kg.macroanalyzer.auth.application.port.out.NewEmailToken;
import com.kg.macroanalyzer.auth.application.port.out.PasswordHasherPort;
import com.kg.macroanalyzer.auth.application.port.out.UserCredential;
import com.kg.macroanalyzer.auth.domain.AccountRules;
import com.kg.macroanalyzer.auth.domain.EmailTokenPurpose;
import com.kg.macroanalyzer.auth.domain.InvalidCredentialsException;
import com.kg.macroanalyzer.auth.domain.InvalidEmailTokenException;
import com.kg.macroanalyzer.auth.domain.InvalidSessionException;
import com.kg.macroanalyzer.auth.domain.Role;
import com.kg.macroanalyzer.auth.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements AuthCommands {

    private static final String REGISTER_PATH = "/konto/registrera";
    private static final String PASSWORD_RESET_PATH = "/konto/aterstall-losenord";

    private final AuthRepositoryPort repository;
    private final PasswordHasherPort passwordHasher;
    private final AccessTokenPort accessTokens;
    private final MailPort mail;
    private final Clock clock;
    private final Duration sessionTtl;
    private final Duration emailTokenTtl;
    private final Duration emailTokenResendInterval;
    private final String publicUrl;
    private final String adminEmail;
    private final String hashOfNoPassword;

    public AuthService(
            AuthRepositoryPort repository,
            PasswordHasherPort passwordHasher,
            AccessTokenPort accessTokens,
            MailPort mail,
            Clock clock,
            @Value("${auth.session-ttl}") Duration sessionTtl,
            @Value("${auth.email-token-ttl}") Duration emailTokenTtl,
            @Value("${auth.email-token-resend-interval}") Duration emailTokenResendInterval,
            @Value("${auth.public-url}") String publicUrl,
            @Value("${auth.admin-email}") String adminEmail
    ) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.accessTokens = accessTokens;
        this.mail = mail;
        this.clock = clock;
        this.sessionTtl = sessionTtl;
        this.emailTokenTtl = emailTokenTtl;
        this.emailTokenResendInterval = emailTokenResendInterval;
        this.publicUrl = publicUrl.replaceAll("/+$", "");
        this.adminEmail = adminEmail.strip().toLowerCase(Locale.ROOT);
        this.hashOfNoPassword = passwordHasher.hash(SecretTokens.generate());
    }

    @Override
    public void requestRegistration(String email) {
        final var normalized = AccountRules.normalizedEmail(email);
        issueEmailToken(
                normalized,
                !repository.userExists(normalized),
                EmailTokenPurpose.REGISTER,
                REGISTER_PATH,
                "Skapa ditt konto på Macro Hub",
                "Öppna länken för att välja namn och lösenord. Den gäller i %d minuter.");
    }

    @Override
    public void completeRegistration(String emailToken, String displayName, String password) {
        final var name = AccountRules.validDisplayName(displayName);
        final var account = new NewAccount(
                UUID.randomUUID(), name, passwordHasher.hash(AccountRules.validPassword(password)));
        final var registered = emailToken != null
                && repository.registerWithToken(
                        SecretTokens.hash(emailToken), clock.instant(), account, this::roleOf);
        if (!registered) {
            throw new InvalidEmailTokenException();
        }
    }

    @Override
    public void requestPasswordReset(String email) {
        final var normalized = AccountRules.normalizedEmail(email);
        issueEmailToken(
                normalized,
                repository.userExists(normalized),
                EmailTokenPurpose.PASSWORD_RESET,
                PASSWORD_RESET_PATH,
                "Återställ ditt lösenord på Macro Hub",
                "Öppna länken för att välja ett nytt lösenord. Den gäller i %d minuter.");
    }

    @Override
    public void completePasswordReset(String emailToken, String password) {
        final var hash = passwordHasher.hash(AccountRules.validPassword(password));
        final var reset = emailToken != null
                && repository.resetPasswordWithToken(
                        SecretTokens.hash(emailToken), clock.instant(), hash);
        if (!reset) {
            throw new InvalidEmailTokenException();
        }
    }

    @Override
    public Login login(String email, String password) {
        final var credential = email == null ? Optional.<UserCredential>empty()
                : repository.findCredential(email.strip().toLowerCase(Locale.ROOT));
        // Hashed also for an unknown address, so the response time doesn't tell which
        // addresses have accounts
        final var hash = credential.map(UserCredential::passwordHash).orElse(hashOfNoPassword);
        final var matches = password != null && passwordHasher.matches(password, hash);
        if (credential.isEmpty() || !matches) {
            throw new InvalidCredentialsException();
        }

        final var now = clock.instant();
        final var user = credential.get().user();
        final var sessionToken = SecretTokens.generate();
        repository.saveSession(
                SecretTokens.hash(sessionToken), user.id(), now, now.plus(sessionTtl));

        return new Login(
                sessionToken, sessionTtl, new Authenticated(accessTokens.issue(user, now), user));
    }

    @Override
    public Authenticated refresh(String sessionToken) {
        final var now = clock.instant();
        final User user = sessionToken == null ? null
                : repository.findUserOfSession(SecretTokens.hash(sessionToken), now).orElse(null);
        if (user == null) {
            throw new InvalidSessionException();
        }

        return new Authenticated(accessTokens.issue(user, now), user);
    }

    @Override
    public void logout(String sessionToken) {
        if (sessionToken != null) {
            repository.deleteSession(SecretTokens.hash(sessionToken));
        }
    }

    @Override
    public String publicKeys() {
        return accessTokens.publicKeys();
    }

    private Role roleOf(String email) {
        return email.equals(adminEmail) ? Role.ADMIN : Role.USER;
    }

    // The token is stored also when nothing is mailed, so both cases take the same database
    // work and the response time doesn't tell which addresses have accounts. Its value is
    // discarded then, so that row can never be used
    private void issueEmailToken(
            String email,
            boolean deliver,
            EmailTokenPurpose purpose,
            String path,
            String subject,
            String instruction
    ) {
        final var now = clock.instant();
        final var token = SecretTokens.generate();
        final var saved = repository.saveEmailTokenUnlessIssuedSince(
                new NewEmailToken(
                        SecretTokens.hash(token), email, purpose, now, now.plus(emailTokenTtl)),
                now.minus(emailTokenResendInterval));
        if (saved && deliver) {
            // In the fragment, which browsers don't send: the gateway's access log and Loki
            // never see the token
            final var link = "%s%s#token=%s".formatted(publicUrl, path, token);
            mail.send(
                    email,
                    subject,
                    "%s\n\n%s\n".formatted(instruction.formatted(emailTokenTtl.toMinutes()), link));
        }
    }

}
