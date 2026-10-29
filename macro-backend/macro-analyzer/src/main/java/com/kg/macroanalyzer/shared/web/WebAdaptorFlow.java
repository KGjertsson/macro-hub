package com.kg.macroanalyzer.shared.web;

import com.kg.macroanalyzer.shared.web.ConnectionBuilder;
import com.kg.macroanalyzer.shared.web.Getter;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.net.HttpURLConnection;

public record WebAdaptorFlow<Config, Response>(
        ConnectionBuilder<Config, HttpURLConnection> connectionBuilder,
        Getter<HttpURLConnection, String> getter,
        ResponseParser<String, Response> responseParser
) {

    public Response fetch(Config config) throws ScrapeException {
        final var connection = connectionBuilder.buildConnection(config);
        final var externalResponse = getter.get(connection);

        return responseParser.parse(externalResponse);
    }

}
