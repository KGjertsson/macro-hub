package com.kg.macroanalyzer.adaptors.webadaptorflow;

import com.kg.macroanalyzer.adaptors.webadaptorflow.connectionbuilder.ConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptorflow.getter.Getter;
import com.kg.macroanalyzer.adaptors.webadaptorflow.responserparser.ResponseParser;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;

import java.net.HttpURLConnection;

public record WebAdaptorFlow<Config, InternalResponse>(
        ConnectionBuilder<Config, HttpURLConnection> connectionBuilder,
        Getter<HttpURLConnection, String> getter,
        ResponseParser<String, InternalResponse> responseParser
) {

    public InternalResponse fetch(Config config) throws ScrapeException {
        final var connection = connectionBuilder.buildConnection(config);
        final var externalResponse = getter.get(connection);

        return responseParser.parse(externalResponse);
    }

}
