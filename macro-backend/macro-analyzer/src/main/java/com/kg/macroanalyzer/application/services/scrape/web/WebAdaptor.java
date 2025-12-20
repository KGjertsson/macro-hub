package com.kg.macroanalyzer.application.services.scrape.web;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.stream.Stream;

@Slf4j
@Component
public abstract class WebAdaptor {

    public Stream<MacroPoint> getMacroPoints(SeriesConfig seriesConfig) throws ScrapeException {
        final var response = getHTTP(seriesConfig);

        return parseResponse(response);
    }

    private String getHTTP(SeriesConfig seriesConfig) throws ScrapeException {
        try {
            final var connection = buildConnection(seriesConfig);

            final int status = connection.getResponseCode();

            InputStream is = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (is == null) {
                // Fallback to inputStream if errorStream is null
                is = connection.getInputStream();
            }

            // Handle gzip compression if present
            final var contentEncoding = connection.getContentEncoding();
            if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line).append('\n');
                }
            }

            final String response = responseBuilder.toString();

            // Close the connection
            connection.disconnect();

            if (status >= 400) {
                throw new ScrapeException("HTTP " + status + " from upstream: " + response);
            }

            return response;
        } catch (IOException | URISyntaxException exception) {
            throw new ScrapeException(exception.getMessage());
        }
    }

    protected abstract HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws IOException, URISyntaxException;

    protected abstract Stream<MacroPoint> parseResponse(String response) throws ScrapeException;

}
