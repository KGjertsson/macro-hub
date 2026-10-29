package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ConnectionBuilder;
import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public record RiksdagenConnectionBuilder() implements ConnectionBuilder<SeriesConfig, HttpURLConnection> {

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 120_000;

    @Override
    public HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws ScrapeException {
        return open(seriesConfig.scrapeUrl());
    }

    public static HttpURLConnection open(String rawUrl) throws ScrapeException {
        try {
            final var url = new URI(rawUrl).toURL();
            final var connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.7");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Referer", "https://data.riksdagen.se/personlista/");
            connection.setRequestProperty("Sec-Fetch-Dest", "document");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestProperty("Sec-Fetch-Site", "same-origin");
            connection.setRequestProperty("Sec-Fetch-User", "?1");
            connection.setRequestProperty("Sec-GPC", "1");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36");
            connection.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"142\", \"Brave\";v=\"142\", \"Not_A Brand\";v=\"99\"");
            connection.setRequestProperty("sec-ch-ua-mobile", "?0");
            connection.setRequestProperty("sec-ch-ua-platform", "\"macOS\"");

            return connection;
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            final var msg = "Failed to build Riksdagen connection: " + e.getMessage();

            throw new ScrapeException(msg);
        }
    }

}
