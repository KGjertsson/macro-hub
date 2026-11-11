package com.kg.macroanalyzer.auth.adaptors.http;

import com.kg.macroanalyzer.auth.application.port.out.MailPort;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthFlowHttpTest {

    private static final String PATH = "/macro-analyzer/auth";
    private static final String PASSWORD = "correct-horse-battery";
    private static final String OTHER_PASSWORD = "another-long-password";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @MockitoBean
    MailPort mail;

    @Value("${local.server.port}")
    int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldCreateAnAccount_fromTheMailedLink_andIssueAVerifiableToken() throws Exception {
        // given
        final var email = newEmail();
        register(email, uniqueName());

        // when
        final var login = login(email, PASSWORD);

        // then
        assertEquals(200, login.statusCode());
        final var cookie = login.headers().firstValue("Set-Cookie").orElseThrow();
        assertTrue(cookie.contains("HttpOnly"), cookie);
        assertTrue(cookie.contains("Secure"), cookie);
        assertTrue(cookie.contains("SameSite=Strict"), cookie);
        assertTrue(cookie.contains("Path=/macro-analyzer/auth"), cookie);
        assertEquals("no-store", login.headers().firstValue("Cache-Control").orElseThrow());

        final var body = JSON.readTree(login.body());
        assertEquals("USER", body.get("user").get("role").asString());
        assertEquals(600, body.get("expiresInSeconds").asInt());

        final var jwt = SignedJWT.parse(body.get("accessToken").asString());
        final var keys = JWKSet.parse(get(PATH + "/jwks.json").body());
        final var key = keys.getKeyByKeyId(jwt.getHeader().getKeyID());
        assertFalse(key.isPrivate());
        assertTrue(jwt.verify(new ECDSAVerifier(key.toECKey())));
        final var claims = jwt.getJWTClaimsSet();
        assertEquals(body.get("user").get("id").asString(), claims.getSubject());
        assertEquals("macro-hub-auth", claims.getIssuer());
        assertEquals(List.of("macro-hub"), claims.getAudience());
        assertEquals(List.of("USER"), claims.getStringListClaim("roles"));
        assertTrue(claims.getExpirationTime().after(new Date()));
    }

    @Test
    void shouldLinkToThePublicUrl_withTheTokenInTheFragment() throws Exception {
        // given
        final var email = newEmail();

        // when
        postJson(PATH + "/register", emailBody(email));

        // then
        assertTrue(
                mailedBody(email).contains("http://localhost:8081/konto/registrera#token="),
                mailedBody(email));
    }

    @Test
    void shouldGrantAdmin_toTheConfiguredAddress() throws Exception {
        // given
        register("admin@example.test", "Administratör");

        // when
        final var login = JSON.readTree(login("admin@example.test", PASSWORD).body());

        // then
        assertEquals("ADMIN", login.get("user").get("role").asString());
    }

    @Test
    void shouldRefreshAndThenRevoke_theSession() throws Exception {
        // given
        final var email = newEmail();
        register(email, uniqueName());
        final var cookie = sessionCookie(login(email, PASSWORD));

        // when
        final var refreshed = postWithCookie(PATH + "/token", cookie);
        final var logout = postWithCookie(PATH + "/logout", cookie);
        final var afterLogout = postWithCookie(PATH + "/token", cookie);

        // then
        assertEquals(200, refreshed.statusCode());
        assertFalse(JSON.readTree(refreshed.body()).get("accessToken").asString().isEmpty());
        assertEquals(204, logout.statusCode());
        assertTrue(logout.headers().firstValue("Set-Cookie").orElseThrow().contains("Max-Age=0"));
        assertEquals(401, afterLogout.statusCode());
    }

    @Test
    void shouldRejectARefresh_withoutACookie() throws Exception {
        assertEquals(401, postJson(PATH + "/token", "").statusCode());
    }

    @Test
    void shouldAnswerAlike_forAWrongPasswordAndAnUnknownAddress() throws Exception {
        // given
        final var email = newEmail();
        register(email, uniqueName());

        // when
        final var wrongPassword = login(email, OTHER_PASSWORD);
        final var unknownAddress = login(newEmail(), PASSWORD);

        // then
        assertEquals(401, wrongPassword.statusCode());
        assertEquals(401, unknownAddress.statusCode());
        assertEquals(wrongPassword.body(), unknownAddress.body());
    }

    @Test
    void shouldNotMail_anAddressThatAlreadyHasAnAccount() throws Exception {
        // given
        final var email = newEmail();
        register(email, uniqueName());

        // when
        final var again = postJson(PATH + "/register", emailBody(email));

        // then
        assertEquals(202, again.statusCode());
        verify(mail, times(1)).send(eq(email), anyString(), anyString());
    }

    @Test
    void shouldSendOneMail_forRepeatedRequestsWithinTheInterval() throws Exception {
        // given
        final var email = newEmail();

        // when
        postJson(PATH + "/register", emailBody(email));
        final var second = postJson(PATH + "/register", emailBody(email));

        // then
        assertEquals(202, second.statusCode());
        verify(mail, times(1)).send(eq(email), anyString(), anyString());
    }

    @Test
    void shouldNotMail_aPasswordResetForAnUnknownAddress() throws Exception {
        // given
        final var email = newEmail();

        // when
        final var response = postJson(PATH + "/password-reset", emailBody(email));

        // then
        assertEquals(202, response.statusCode());
        verify(mail, never()).send(eq(email), anyString(), anyString());
    }

    @Test
    void shouldAcceptALinkOnlyOnce() throws Exception {
        // given
        final var email = newEmail();
        postJson(PATH + "/register", emailBody(email));
        final var token = mailedToken(email);
        assertEquals(204, complete(token, uniqueName()).statusCode());

        // when
        final var again = complete(token, uniqueName());

        // then
        assertEquals(400, again.statusCode());
        assertEquals(
                "invalid_or_expired_token", JSON.readTree(again.body()).get("error").asString());
    }

    @Test
    void shouldKeepTheLinkUsable_whenTheDisplayNameIsTaken() throws Exception {
        // given
        final var name = uniqueName();
        register(newEmail(), name);
        final var email = newEmail();
        postJson(PATH + "/register", emailBody(email));
        final var token = mailedToken(email);

        // when
        final var taken = complete(token, name.toUpperCase());
        final var retried = complete(token, uniqueName());

        // then
        assertEquals(409, taken.statusCode());
        assertEquals(204, retried.statusCode());
    }

    @Test
    void shouldRejectAShortPassword() throws Exception {
        // when
        final var response = postJson(PATH + "/register/complete",
                JSON.writeValueAsString(
                        new CompleteRegistrationRequest("token", uniqueName(), "short")));

        // then
        assertEquals(400, response.statusCode());
        assertEquals("invalid_password", JSON.readTree(response.body()).get("error").asString());
    }

    @Test
    void shouldReplaceThePassword_andEndEverySession_onAReset() throws Exception {
        // given
        final var email = newEmail();
        register(email, uniqueName());
        final var cookie = sessionCookie(login(email, PASSWORD));
        clearInvocations(mail);
        postJson(PATH + "/password-reset", emailBody(email));

        // when
        final var reset = postJson(PATH + "/password-reset/complete",
                JSON.writeValueAsString(
                        new CompletePasswordResetRequest(mailedToken(email), OTHER_PASSWORD)));

        // then
        assertEquals(204, reset.statusCode());
        assertEquals(401, postWithCookie(PATH + "/token", cookie).statusCode());
        assertEquals(401, login(email, PASSWORD).statusCode());
        assertEquals(200, login(email, OTHER_PASSWORD).statusCode());
    }

    @Test
    void shouldNotAcceptARegistrationLink_asAPasswordReset() throws Exception {
        // given
        final var email = newEmail();
        postJson(PATH + "/register", emailBody(email));

        // when
        final var response = postJson(PATH + "/password-reset/complete",
                JSON.writeValueAsString(
                        new CompletePasswordResetRequest(mailedToken(email), OTHER_PASSWORD)));

        // then
        assertEquals(400, response.statusCode());
    }

    private void register(String email, String displayName) throws Exception {
        postJson(PATH + "/register", emailBody(email));
        assertEquals(204, complete(mailedToken(email), displayName).statusCode());
    }

    private HttpResponse<String> complete(String token, String displayName) throws Exception {
        return postJson(PATH + "/register/complete",
                JSON.writeValueAsString(
                        new CompleteRegistrationRequest(token, displayName, PASSWORD)));
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        return postJson(
                PATH + "/login", JSON.writeValueAsString(new LoginRequest(email, password)));
    }

    private String emailBody(String email) {
        return JSON.writeValueAsString(new EmailRequest(email));
    }

    private String mailedToken(String email) {
        return mailedBody(email).split("#token=")[1].strip();
    }

    private String mailedBody(String email) {
        final var body = ArgumentCaptor.forClass(String.class);
        verify(mail, atLeastOnce()).send(eq(email), anyString(), body.capture());

        return body.getValue();
    }

    private static String sessionCookie(HttpResponse<String> login) {
        return login.headers().firstValue("Set-Cookie").orElseThrow().split(";")[0];
    }

    private static String newEmail() {
        return UUID.randomUUID() + "@example.test";
    }

    private static String uniqueName() {
        return "Namn " + UUID.randomUUID().toString().substring(0, 8);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return httpClient.send(
                HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String body) throws Exception {
        final var request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postWithCookie(String path, String cookie) throws Exception {
        final var request = HttpRequest.newBuilder(uri(path))
                .header("Cookie", cookie)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

}
