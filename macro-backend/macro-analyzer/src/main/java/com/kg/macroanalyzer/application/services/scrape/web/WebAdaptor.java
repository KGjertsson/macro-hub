package com.kg.macroanalyzer.application.services.scrape.web;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
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

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            // Get the response as a string
            String response = responseBuilder.toString();

            // Close the connection
            connection.disconnect();

            return response;
        } catch (IOException | URISyntaxException exception) {
            throw new ScrapeException(exception.getMessage());
        }
    }

    protected abstract HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws IOException, URISyntaxException;

    protected abstract Stream<MacroPoint> parseResponse(String response) throws ScrapeException;

}
