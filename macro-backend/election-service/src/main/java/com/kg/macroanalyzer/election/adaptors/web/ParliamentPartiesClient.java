package com.kg.macroanalyzer.election.adaptors.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// See contracts/parliament-parties.json for the fields election relies on
public class ParliamentPartiesClient implements AutoCloseable {

    private static final String PARTIES_PATH = "/macro-analyzer/parliament/parties";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private final ExecutorService exchanges = Executors.newThreadPerTaskExecutor(
            Thread.ofPlatform().name("party-list-fetch").daemon().factory()
    );
    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final Duration timeout;

    public ParliamentPartiesClient(
            RestClient.Builder restClientBuilder,
            String parliamentUrl,
            Duration timeout,
            JsonMapper jsonMapper
    ) {
        this.restClient = restClientBuilder
                .baseUrl(parliamentUrl.replaceAll("/+$", ""))
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
        this.jsonMapper = jsonMapper;
        this.timeout = timeout;
    }

    public Map<String, String> fetchPartyColors()
            throws IOException, InterruptedException, TimeoutException {
        // The deadline is on the future, as no client timeout covers a body that stalls after the
        // headers
        final var exchange = exchanges.submit(this::exchange);
        try {
            return exchange.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        } finally {
            exchange.cancel(true);
        }
    }

    private Map<String, String> exchange() {
        return restClient.get().uri(PARTIES_PATH).exchange(
                (request, response) -> switch (response.getStatusCode().value()) {
                    case 200 -> parse(
                            new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
                    case 204 -> Map.of();
                    default -> throw new IOException(
                            "HTTP " + response.getStatusCode().value());
                });
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
        exchanges.shutdownNow();
        httpClient.shutdownNow();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PartyEntry(String code, String color) {
    }

}
