package com.kg.macroanalyzer.adaptors.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public record FedConnectionBuilder(
        String subscriptionKey
) implements ConnectionBuilder<SeriesConfig, HttpURLConnection> {

    @Override
    public HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws ScrapeException {
        try {
            final var endpointUrl = String.format(
                    "%s&api_key=%s&file_type=json",
                    seriesConfig.scrapeUrl(),
                    subscriptionKey
            );
            final var url = new URI(endpointUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            return connection;
        } catch (IOException | URISyntaxException e) {
            final var msg = "Failed to build Fed connection: " + e.getMessage();

            throw new ScrapeException(msg);
        }
    }

}
