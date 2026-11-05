package com.kg.macroanalyzer.acceptance;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public record Sources(Gateway wireMockAdmin) {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final int LOWER_THAN_THE_DEFAULT_PRIORITY = 9;

    public void serveJson(String path, String fixture) {
        serveJson(path, Map.of(), fixture);
    }

    public void serveJson(String path, Map<String, String> query, String fixture) {
        stub(path, query, jsonResponse(readFixture(fixture)));
    }

    public void serveJsonBody(String path, String body) {
        serveJsonBody(path, Map.of(), body);
    }

    public void serveJsonBody(String path, Map<String, String> query, String body) {
        stub(path, query, jsonResponse(body));
    }

    public void serveJsonForOtherPathsMatching(String pathRegex, String fixture) {
        postMapping(Map.of(
                "priority", LOWER_THAN_THE_DEFAULT_PRIORITY,
                "request", Map.of("method", "GET", "urlPathPattern", pathRegex),
                "response", jsonResponse(readFixture(fixture))));
    }

    public void serveText(String path, String body) {
        stub(path, Map.of(), Map.of(
                "status", 200,
                "headers", Map.of("Content-Type", "text/plain; charset=utf-8"),
                "body", body));
    }

    public void serveBytes(String path, byte[] body) {
        stub(path, Map.of(), Map.of(
                "status", 200,
                "headers", Map.of("Content-Type", "application/octet-stream"),
                "base64Body", Base64.getEncoder().encodeToString(body)));
    }

    public int requestCount(String path) {
        return requestCount(path, Map.of());
    }

    public int requestCount(String path, Map<String, String> query) {
        final var response = wireMockAdmin.postJson("/__admin/requests/count", JSON.writeValueAsString(request(path, query)));

        return JSON.readTree(response.body()).get("count").asInt();
    }

    private Map<String, Object> jsonResponse(String body) {
        return Map.of(
                "status", 200,
                "headers", Map.of("Content-Type", "application/json"),
                "body", body);
    }

    private void stub(String path, Map<String, String> query, Map<String, Object> response) {
        postMapping(Map.of(
                "request", request(path, query),
                "response", response));
    }

    private void postMapping(Map<String, Object> mapping) {
        final var answer = wireMockAdmin.postJson("/__admin/mappings", JSON.writeValueAsString(mapping));
        if (answer.statusCode() != 201) {
            throw new IllegalStateException("WireMock refused the stub: " + answer.body());
        }
    }

    private Map<String, Object> request(String path, Map<String, String> query) {
        final var queryParameters = query.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, parameter -> Map.of("equalTo", parameter.getValue())));

        return Map.of("method", "GET", "urlPath", path, "queryParameters", queryParameters);
    }

    public static String readFixture(String fixture) {
        final var resource = "sources/" + fixture;
        try (var stream = Sources.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("No fixture on the classpath: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
