package com.kg.macroanalyzer.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class WebUtils {

    @Value("${riskbanken.prod.subscription.key}")
    private String subscriptionKey;

    public Stream<MacroPoint> getMacroPoints(SeriesConfig seriesConfig) throws ScrapeException {
        try {
            final var response = getHTTP(seriesConfig.scrapeUrl());
            final var objectMapper = createObjectMapper();

            JavaType collectionType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ScrapedResponse.class);
            final List<ScrapedResponse> resultList = objectMapper.readValue(response, collectionType);

            return resultList.stream()
                    .map(ScrapedResponse::toMacroPoint);
        } catch (IOException ioException) {
            final var msgRaw = "Encountered IOException when scraping %s, e=%s";
            final var msgFormatted = msgRaw.formatted(seriesConfig.displayName(), ioException);
            log.error(msgFormatted);

            throw new ScrapeException(msgFormatted);
        }
    }

    private ObjectMapper createObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private String getHTTP(String endpointUrl) {
        try {
            final var url = new URI(endpointUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (subscriptionKey != null) {
                // Add custom header "Ocp-Apim-Subscription-Key" with its value
                connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
            }

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
        } catch (IOException | URISyntaxException ioException) {
            throw new RuntimeException(ioException.getMessage());
        }
    }

    private record ScrapedResponse(@NonNull Double value, @NonNull String date) {

        public MacroPoint toMacroPoint() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            return MacroPoint.builder()
                    .value(value)
                    .date(LocalDate.parse(date, formatter))
                    .build();
        }

    }

}
