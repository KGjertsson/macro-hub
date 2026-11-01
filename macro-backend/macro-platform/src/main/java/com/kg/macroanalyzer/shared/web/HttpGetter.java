package com.kg.macroanalyzer.shared.web;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpGetter implements Getter<HttpURLConnection, String> {

    @Override
    public String get(HttpURLConnection connection) throws ScrapeException {
        try {
            final int status = HttpResponses.connect(connection);
            final String response = HttpResponses.readText(HttpResponses.getInputStream(status, connection));

            // Close the connection
            connection.disconnect();

            if (status >= 400) {
                throw new ScrapeException("HTTP " + status + " from upstream: " + response);
            }

            return response;
        } catch (IOException exception) {
            throw new ScrapeException(exception.getMessage());
        }
    }

}
