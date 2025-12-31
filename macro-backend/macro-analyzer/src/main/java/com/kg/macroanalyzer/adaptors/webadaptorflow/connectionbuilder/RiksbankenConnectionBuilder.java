package com.kg.macroanalyzer.adaptors.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public record RiksbankenConnectionBuilder(
        String subscriptionKey
) implements ConnectionBuilder<SeriesConfig, HttpURLConnection> {

    @Override
    public HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws ScrapeException {
        try {
            final var endpointUrl = seriesConfig.scrapeUrl();
            final var url = new URI(endpointUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (subscriptionKey != null) {
                // Add custom header "Ocp-Apim-Subscription-Key" with its value
                connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
            }

            return connection;
        } catch (IOException | URISyntaxException e) {
            final var msg = "Failed to build Riksbanken connection: " + e.getMessage();

            throw new ScrapeException(msg);
        }
    }

}
