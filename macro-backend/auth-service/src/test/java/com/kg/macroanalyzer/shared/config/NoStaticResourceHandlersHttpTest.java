package com.kg.macroanalyzer.shared.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins spring.web.resources.add-mappings=false: with Boot's /** resource handler registered, a
 * file on a static location is served and an unknown path answers CORS requests (200 on a
 * preflight, 403 on other methods) instead of 404. The file is what shows that the handler is
 * gone, not merely empty.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NoStaticResourceHandlersHttpTest {

    private static final String STATIC_FILE = "static-file.txt";
    private static final String ORIGIN = "http://example.test";

    @TempDir
    static Path staticLocation;

    @Value("${local.server.port}")
    int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @DynamicPropertySource
    static void useTempDirAsStaticLocation(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.web.resources.static-locations", () -> staticLocation.toUri().toString());
    }

    @BeforeAll
    static void createStaticFile() throws IOException {
        Files.writeString(staticLocation.resolve(STATIC_FILE), "must not be served");
    }

    @Test
    void shouldNotServeFile_onStaticLocation() throws Exception {
        // given
        final var request = HttpRequest.newBuilder(uri("/" + STATIC_FILE)).GET().build();

        // when
        final var response = send(request);

        // then
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnNotFound_onPreflightForUnknownPath() throws Exception {
        // given
        final var request = HttpRequest.newBuilder(uri("/macro-analyzer/unknown"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .build();

        // when
        final var response = send(request);

        // then
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnNotFound_onPatchWithOriginForUnknownPath() throws Exception {
        // given
        final var request = HttpRequest.newBuilder(uri("/macro-analyzer/unknown"))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Origin", ORIGIN)
                .build();

        // when
        final var response = send(request);

        // then
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnNotFound_onOptionsWithOriginForUnknownPath() throws Exception {
        // given
        final var request = HttpRequest.newBuilder(uri("/macro-analyzer/unknown"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", ORIGIN)
                .build();

        // when
        final var response = send(request);

        // then
        assertEquals(404, response.statusCode());
    }

    private HttpResponse<String> send(HttpRequest request)
            throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

}
