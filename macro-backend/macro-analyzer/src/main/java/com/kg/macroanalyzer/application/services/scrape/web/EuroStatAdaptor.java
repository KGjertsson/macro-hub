package com.kg.macroanalyzer.application.services.scrape.web;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

@Slf4j
@Component
public class EuroStatAdaptor extends WebAdaptor {

    @Override
    protected HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws IOException, URISyntaxException {
        final var endpointUrl = seriesConfig.scrapeUrl();
        final var url = new URI(endpointUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Timeouts
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(60_000);

        // Request headers: be explicit about format and compression
        connection.setRequestProperty("User-Agent", "macro-hub/1.0 (contact: macro-hub)");
        // Align Accept with explicit format parameter to avoid 400/406 from gateway
        final String lower = endpointUrl.toLowerCase();
        if (lower.contains("format=tsv")) {
            // Be liberal: many Eurostat gateways use text/plain for TSV, others use text/tab-separated-values
            // Provide a weighted list to avoid 400/406 due to strict matching
            connection.setRequestProperty(
                    "Accept",
                    "text/plain, text/tab-separated-values;q=0.9, */*;q=0.8"
            );
        } else if (lower.contains("format=json")) {
            connection.setRequestProperty("Accept", "application/json");
        } else {
            // Default: be permissive
            connection.setRequestProperty("Accept", "*/*");
        }
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("Accept-Language", "en");

        return connection;
    }

    @Override
    protected Stream<MacroPoint> parseResponse(String response) throws ScrapeException {
        return Stream.empty();
    }
}
