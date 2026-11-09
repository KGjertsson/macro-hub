package com.kg.macroanalyzer.scrape.adaptors.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ManualScrapePathsHttpTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Value("${local.server.port}")
    int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldAcceptEnqueue_onTheServicesOwnPrefix() throws Exception {
        // An unknown name is accepted and ignored, so this doesn't queue a real scrape
        assertEquals(202, post("/macro-analyzer/scrape/macro-series/enqueue?name=unknown", ADMIN_TOKEN));
    }

    @Test
    void shouldNotAnswer_onTheOldGlobalPaths() throws Exception {
        assertEquals(404, post("/macro-analyzer/enqueue?name=unknown", null));
    }

    @Test
    void shouldReject_aRequestWithoutAToken() throws Exception {
        assertEquals(401, post("/macro-analyzer/scrape/macro-series/enqueue?name=unknown", null));
    }

    @Test
    void shouldReject_anInvalidToken() throws Exception {
        assertEquals(401, post("/macro-analyzer/scrape/macro-series/enqueue?name=unknown", "not-a-token"));
    }

    @Test
    void shouldForbid_aUserWhoIsNotAnAdmin() throws Exception {
        assertEquals(403, post("/macro-analyzer/scrape/macro-series/enqueue?name=unknown", USER_TOKEN));
    }

    // Spring's routing ignores a ;parameter, so it must not be a way around the path match of the filter chain
    @Test
    void shouldNotAccept_aPathParameter() throws Exception {
        assertEquals(400, post("/macro-analyzer/scrape/macro-series/enqueue;x=1?name=unknown", null));
    }

    @BeforeEach
    void knowTheTokens() {
        when(jwtDecoder.decode(anyString())).thenThrow(new BadJwtException("invalid"));
        doReturn(tokenWithRole("ADMIN")).when(jwtDecoder).decode(ADMIN_TOKEN);
        doReturn(tokenWithRole("USER")).when(jwtDecoder).decode(USER_TOKEN);
    }

    private static Jwt tokenWithRole(String role) {
        return Jwt.withTokenValue("token").header("alg", "ES256").subject("user").claim("roles", List.of(role)).build();
    }

    private int post(String path, String token) throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return httpClient.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

}
