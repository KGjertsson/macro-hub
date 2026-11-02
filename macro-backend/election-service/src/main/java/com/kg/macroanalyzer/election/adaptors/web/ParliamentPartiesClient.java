package com.kg.macroanalyzer.election.adaptors.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Reads GET /macro-analyzer/parliament/parties. See contracts/parliament-parties.json for the fields election relies on
public class ParliamentPartiesClient implements AutoCloseable {

    private static final String PARTIES_PATH = "/macro-analyzer/parliament/parties";

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private final JsonMapper jsonMapper;
    private final HttpRequest request;
    private final Duration timeout;

    public ParliamentPartiesClient(String parliamentUrl, Duration timeout, JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.request = HttpRequest.newBuilder(URI.create(parliamentUrl.replaceAll("/+$", "") + PARTIES_PATH)).GET().build();
        this.timeout = timeout;
    }

    /**
     * @return colour by party code, empty on the endpoint's 204 for an empty list
     * @throws TimeoutException the whole exchange took longer than the timeout
     * @throws IOException      the call failed, or the answer isn't a list of parties with a code and a colour
     */
    public Map<String, String> fetchPartyColors() throws IOException, InterruptedException, TimeoutException {
        // The deadline is on the future, as HttpRequest.timeout doesn't cover a body that stalls after the headers
        final var exchange = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            final var response = exchange.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return switch (response.statusCode()) {
                case 200 -> parse(response.body());
                case 204 -> Map.of();
                default -> throw new IOException("HTTP " + response.statusCode());
            };
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        } finally {
            exchange.cancel(true);
        }
    }

    private Map<String, String> parse(String body) throws IOException {
        final PartyEntry[] parties;
        try {
            parties = jsonMapper.readValue(body, PartyEntry[].class);
        } catch (JacksonException e) {
            throw new IOException("Not a list of parties", e);
        }
        if (parties == null) {
            throw new IOException("Not a list of parties: " + body);
        }

        final var colors = new HashMap<String, String>();
        for (final var party : parties) {
            if (party == null || isBlank(party.code()) || isBlank(party.color())) {
                throw new IOException("A party without a code or colour: " + party);
            }
            colors.putIfAbsent(party.code(), party.color());
        }
        return Map.copyOf(colors);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public void close() {
        httpClient.shutdownNow();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PartyEntry(String code, String color) {
    }

}
