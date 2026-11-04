package com.kg.macroanalyzer.acceptance;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public record Sources(Gateway wireMockAdmin) {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public void serveJson(String path, String fixture) {
        final var mapping = JSON.writeValueAsString(Map.of(
                "request", Map.of("method", "GET", "urlPath", path),
                "response", Map.of(
                        "status", 200,
                        "headers", Map.of("Content-Type", "application/json"),
                        "body", readFixture(fixture))));

        final var response = wireMockAdmin.postJson("/__admin/mappings", mapping);
        if (response.statusCode() != 201) {
            throw new IllegalStateException("WireMock refused the stub: " + response.body());
        }
    }

    public int requestCount(String path) {
        final var filter = JSON.writeValueAsString(Map.of("method", "GET", "urlPath", path));
        final var response = wireMockAdmin.postJson("/__admin/requests/count", filter);

        return JSON.readTree(response.body()).get("count").asInt();
    }

    private String readFixture(String fixture) {
        final var resource = "sources/" + fixture;
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("No fixture on the classpath: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
