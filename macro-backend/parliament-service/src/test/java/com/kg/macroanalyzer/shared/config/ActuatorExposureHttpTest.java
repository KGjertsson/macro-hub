package com.kg.macroanalyzer.shared.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ActuatorExposureHttpTest {

    private static final String APPLICATION_LABEL = "application=\"parliament\"";

    @Value("${local.server.port}")
    int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldServeMetrics_withApplicationLabel() throws Exception {
        // given
        get("/actuator/health");

        // when
        final var response = get("/actuator/prometheus");

        // then
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(APPLICATION_LABEL), response.body());
        assertTrue(response.body().contains("jvm_memory_used_bytes"));
        assertTrue(response.body().contains("hikaricp_connections_max"));
        assertTrue(response.body().contains("http_server_requests_seconds_bucket"));
    }

    @Test
    void shouldServeScrapeMetrics_beforeAnyScrape() throws Exception {
        // when
        final var metrics = get("/actuator/prometheus").body();

        // then
        assertTrue(metrics.contains(
                "macro_scrape_items_total{application=\"parliament\",flow=\"MEMBER_OF_PARLIAMENT\","
                        + "result=\"failed\"} 0"), metrics);
        assertTrue(metrics.contains(
                "macro_scrape_items_total{application=\"parliament\",flow=\"MEMBER_OF_PARLIAMENT\","
                        + "result=\"success\"} 0"), metrics);
        assertTrue(metrics.contains(
                "macro_scrape_items_total{application=\"parliament\",flow=\"unknown\","
                        + "result=\"failed\"} 0"), metrics);
        assertTrue(metrics.contains(
                "macro_scrape_item_duration_seconds_bucket{application=\"parliament\","
                        + "flow=\"MEMBER_OF_PARLIAMENT\",le=\"1800.0\"} 0"), metrics);
        assertTrue(
                metrics.contains("macro_scrape_queue_pending{application=\"parliament\"}"),
                metrics
        );
    }

    @Test
    void shouldServeHealth() throws Exception {
        // when
        final var response = get("/actuator/health");

        // then
        assertEquals(200, response.statusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "env", "beans", "configprops", "metrics", "loggers", "heapdump", "threaddump"
    })
    void shouldNotExpose_otherEndpoints(String endpoint) throws Exception {
        // when
        final var response = get("/actuator/" + endpoint);

        // then
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldLabelPathVariableRoutes_withTheirTemplate() throws Exception {
        // given
        final var unknownMember = "0123456789";
        get("/macro-analyzer/parliament/members/" + unknownMember);

        // when
        final var metrics = get("/actuator/prometheus").body();

        // then
        assertTrue(metrics.contains("uri=\"/macro-analyzer/parliament/members/{id}\""), metrics);
        assertFalse(metrics.contains(unknownMember), metrics);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder(uri(path)).GET().build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

}
