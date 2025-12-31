package com.kg.macroanalyzer.adaptors.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public record RiksdagenConnectionBuilder() implements ConnectionBuilder<SeriesConfig, HttpURLConnection> {

    @Override
    public HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws ScrapeException {
        try {
            final var url = new URI(seriesConfig.scrapeUrl()).toURL();
            final var connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
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
        } catch (IOException | URISyntaxException e) {
            final var msg = "Failed to build Riksdagen connection: " + e.getMessage();

            throw new ScrapeException(msg);
        }
    }

}
