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
import static org.junit.jupiter.api.Assertions.assertTrue;

// One journey on its own stack: the mailbox has to exist before auth starts, and the rate limit counts per client address.
// macro-series reads the real auth-service's keys here, unlike in the other journeys
class AuthAcceptanceTest {

    private static final String AUTH = "/macro-analyzer/auth";
    private static final String ENQUEUE = "/macro-analyzer/scrape/macro-series/enqueue?name=unknown";
    private static final String ADMIN = "anna@example.test";
    private static final String USER = "bo@example.test";
    private static final String PASSWORD = "correct-horse-battery";
    private static final Duration MAIL_ARRIVES_WITHIN = Duration.ofSeconds(30);
    private static final Duration ROUTE_ANSWERS_WITHIN = Duration.ofSeconds(30);
    private static final int MORE_THAN_THE_BURST = 15;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void anAdminCanScrapeAUserCannotAndGuessingPasswordsIsRateLimited() {
        try (var stack = AcceptanceStack.startWith(List.of())) {
            // given
            final var mailbox = stack.startMailbox();
            stack.startLater(BackendService.AUTH.withEnvironment(Map.of(
                    "AUTH_ADMIN_EMAIL", ADMIN,
                    "AUTH_MAIL_MODE", "smtp",
                    "SPRING_MAIL_HOST", mailbox.smtpHost(),
                    "SPRING_MAIL_PORT", String.valueOf(mailbox.smtpPort()))));
            stack.startLater(BackendService.MACRO_SERIES.withEnvironment(Map.of(
                    "AUTH_JWKS_URI", BackendService.AUTH.internalUrl() + AUTH + "/jwks.json")));
            final var gateway = stack.gateway();

            // nginx resolves the upstream per request, so the first answers can still be 502. Polled on routes without a
            // rate limit, because limit_req counts those 502s too
            Eventually.assertWithin(ROUTE_ANSWERS_WITHIN, "the route answers",
                    () -> assertEquals(200, gateway.get(AUTH + "/jwks.json").statusCode()));
            Eventually.assertWithin(ROUTE_ANSWERS_WITHIN, "the scrape route answers",
                    () -> assertEquals(401, gateway.postAnonymously(ENQUEUE).statusCode()));

            // when
            final var adminLogin = signUpAndLogIn(gateway, mailbox, ADMIN, "Anna A");
            final var userLogin = signUpAndLogIn(gateway, mailbox, USER, "Bo B");

            // then
            assertEquals("ADMIN", JSON.readTree(adminLogin.body()).get("user").get("role").asString());
            assertEquals("USER", JSON.readTree(userLogin.body()).get("user").get("role").asString());
            assertEquals(202, gateway.postWithBearer(ENQUEUE, accessToken(adminLogin)).statusCode());
            assertEquals(403, gateway.postWithBearer(ENQUEUE, accessToken(userLogin)).statusCode());
            assertEquals(401, gateway.postWithBearer(ENQUEUE, AdminToken.value()).statusCode());
            final var cookie = adminLogin.headers().firstValue("Set-Cookie").orElseThrow().split(";")[0];

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
                    .mapToObj(i -> login(gateway, ADMIN, "wrong-password-" + i).statusCode())
                    .toList();

            // then
            assertTrue(guesses.contains(429), guesses::toString);
            assertEquals(429, gateway.postJson(AUTH + "/login;x=1", "{}").statusCode());
            assertEquals(200, gateway.get(AUTH + "/jwks.json").statusCode());
        }
    }

    private static HttpResponse<String> signUpAndLogIn(Gateway gateway, Mailbox mailbox, String address, String name) {
        assertEquals(202, gateway.postJson(AUTH + "/register", "{\"email\":\"" + address + "\"}").statusCode());
        final var mail = new AtomicReference<String>();
        Eventually.assertWithin(MAIL_ARRIVES_WITHIN, "the mail arrives", () -> mail.set(mailbox.latestTextTo(address)));
        assertTrue(mail.get().contains("http://localhost:8081/konto/registrera#token="), mail.get());
        final var token = mail.get().split("#token=")[1].strip();
        assertEquals(204, gateway.postJson(AUTH + "/register/complete", """
                {"token":"%s","displayName":"%s","password":"%s"}""".formatted(token, name, PASSWORD)).statusCode());

        final var login = login(gateway, address, PASSWORD);
        assertEquals(200, login.statusCode());

        return login;
    }

    private static String accessToken(HttpResponse<String> login) {
        return JSON.readTree(login.body()).get("accessToken").asString();
    }

    private static HttpResponse<String> login(Gateway gateway, String address, String password) {
        return gateway.postJson(AUTH + "/login", """
                {"email":"%s","password":"%s"}""".formatted(address, password));
    }

}
