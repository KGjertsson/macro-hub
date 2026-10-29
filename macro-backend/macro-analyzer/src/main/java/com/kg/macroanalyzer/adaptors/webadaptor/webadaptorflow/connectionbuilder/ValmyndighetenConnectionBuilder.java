package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.shared.web.ConnectionBuilder;
import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public record ValmyndighetenConnectionBuilder() implements ConnectionBuilder<String, HttpURLConnection> {

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 120_000;

    @Override
    public HttpURLConnection buildConnection(String rawUrl) throws ScrapeException {
        try {
            final var connection = (HttpURLConnection) new URI(rawUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "macro-hub");

            return connection;
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            throw new ScrapeException("Failed to build Valmyndigheten connection: " + e.getMessage());
        }
    }

}
