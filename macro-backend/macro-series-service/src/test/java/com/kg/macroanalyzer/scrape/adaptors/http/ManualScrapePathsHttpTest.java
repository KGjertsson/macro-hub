package com.kg.macroanalyzer.scrape.adaptors.http;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The gateway routes /macro-analyzer/scrape/macro-series/ here; the monolith's paths are not this service's. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ManualScrapePathsHttpTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldAcceptEnqueue_onTheServicesOwnPrefix() throws Exception {
        // An unknown name is accepted and ignored, so this doesn't queue a real scrape
        assertEquals(202, post("/macro-analyzer/scrape/macro-series/enqueue?name=unknown"));
    }

    @Test
    void shouldNotAnswer_onTheMonolithsPaths() throws Exception {
        assertEquals(404, post("/macro-analyzer/enqueue?name=unknown"));
    }

    private int post(String path) throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

}
