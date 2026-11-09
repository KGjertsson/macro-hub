package com.kg.macroanalyzer.acceptance;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// One journey on its own stack: the mailbox has to exist before auth starts, and the rate limit counts per client address
class AuthAcceptanceTest {

    private static final String AUTH = "/macro-analyzer/auth";
    private static final String ADDRESS = "anna@example.test";
    private static final String PASSWORD = "correct-horse-battery";
    private static final Duration MAIL_ARRIVES_WITHIN = Duration.ofSeconds(30);
    private static final Duration ROUTE_ANSWERS_WITHIN = Duration.ofSeconds(30);
    private static final int MORE_THAN_THE_BURST = 15;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void aVisitorSignsUpThroughTheMailedLinkLogsInAndIsRateLimitedWhenGuessing() {
        try (var stack = AcceptanceStack.startWith(List.of())) {
            // given
            final var mailbox = stack.startMailbox();
            stack.startLater(BackendService.AUTH.withEnvironment(Map.of(
                    "AUTH_MAIL_MODE", "smtp",
                    "SPRING_MAIL_HOST", mailbox.smtpHost(),
                    "SPRING_MAIL_PORT", String.valueOf(mailbox.smtpPort()))));
            final var gateway = stack.gateway();

            // nginx resolves the upstream per request, so the first answers can still be 502. Polled on a route without a
            // rate limit, because limit_req counts those 502s too
            Eventually.assertWithin(ROUTE_ANSWERS_WITHIN, "the route answers",
                    () -> assertEquals(200, gateway.get(AUTH + "/jwks.json").statusCode()));

            // when
            final var registered = gateway.postJson(AUTH + "/register", "{\"email\":\"" + ADDRESS + "\"}");
            assertEquals(202, registered.statusCode());
            final var mail = new AtomicReference<String>();
            Eventually.assertWithin(MAIL_ARRIVES_WITHIN, "the mail arrives", () -> mail.set(mailbox.latestTextTo(ADDRESS)));
            assertTrue(mail.get().contains("http://localhost:8081/konto/registrera#token="), mail.get());
            final var token = mail.get().split("#token=")[1].strip();

            final var completed = gateway.postJson(AUTH + "/register/complete", """
                    {"token":"%s","displayName":"Anna A","password":"%s"}""".formatted(token, PASSWORD));
            final var login = login(gateway, PASSWORD);

            // then
            assertEquals(204, completed.statusCode());
            assertEquals(200, login.statusCode());
            assertEquals("Anna A", JSON.readTree(login.body()).get("user").get("displayName").asString());
            assertFalse(JSON.readTree(login.body()).get("accessToken").asString().isEmpty());
            final var cookie = login.headers().firstValue("Set-Cookie").orElseThrow().split(";")[0];

            // when
            final var refreshed = gateway.postWithCookie(AUTH + "/token", cookie);
            final var loggedOut = gateway.postWithCookie(AUTH + "/logout", cookie);
            final var afterLogout = gateway.postWithCookie(AUTH + "/token", cookie);

            // then
            assertEquals(200, refreshed.statusCode());
            assertEquals(204, loggedOut.statusCode());
            assertEquals(401, afterLogout.statusCode());

            // when
            final var guesses = IntStream.range(0, MORE_THAN_THE_BURST)
                    .mapToObj(i -> login(gateway, "wrong-password-" + i).statusCode())
                    .toList();

            // then
            assertTrue(guesses.contains(401), guesses::toString);
            assertTrue(guesses.contains(429), guesses::toString);
            assertEquals(429, gateway.postJson(AUTH + "/login;x=1", "{}").statusCode());
            assertEquals(200, gateway.get(AUTH + "/jwks.json").statusCode());
        }
    }

    private static HttpResponse<String> login(Gateway gateway, String password) {
        return gateway.postJson(AUTH + "/login", """
                {"email":"%s","password":"%s"}""".formatted(ADDRESS, password));
    }

}
