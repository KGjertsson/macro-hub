package com.kg.macroanalyzer.parliament.adaptors.web;

import com.kg.macroanalyzer.shared.web.ConnectionBuilder;
import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.net.HttpURLConnection;

public record RiksdagenUrlConnectionBuilder() implements ConnectionBuilder<String, HttpURLConnection> {

    @Override
    public HttpURLConnection buildConnection(String url) throws ScrapeException {
        return RiksdagenConnectionBuilder.open(url);
    }

}
