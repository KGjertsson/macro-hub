package com.kg.macroanalyzer.acceptance.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public record Gateway(String baseUrl) {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Duration SCRAPE_MAY_TAKE_THIS_LONG = Duration.ofMinutes(5);
    private static final String SCRAPE_PATHS = "/macro-analyzer/scrape/";

    public HttpResponse<String> get(String path) {
        return send(request(path).GET());
    }

    public HttpResponse<String> post(String path) {
        return send(request(path).POST(HttpRequest.BodyPublishers.noBody()));
    }

    public HttpResponse<String> postJson(String path, String body) {
        return send(request(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)));
    }

    public HttpResponse<String> postWithCookie(String path, String cookie) {
        return send(request(path)
                .header("Cookie", cookie)
                .POST(HttpRequest.BodyPublishers.noBody()));
    }

    public HttpResponse<String> postAnonymously(String path) {
        return send(anonymousRequest(path).POST(HttpRequest.BodyPublishers.noBody()));
    }

    public HttpResponse<String> postWithBearer(String path, String token) {
        return send(anonymousRequest(path)
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody()));
    }

    private HttpRequest.Builder request(String path) {
        final var request = anonymousRequest(path);

        return path.startsWith(SCRAPE_PATHS)
                ? request.header("Authorization", "Bearer " + AdminToken.value())
                : request;
    }

    private HttpRequest.Builder anonymousRequest(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(SCRAPE_MAY_TAKE_THIS_LONG);
    }

    private HttpResponse<String> send(HttpRequest.Builder request) {
        try {
            return HTTP_CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

}
