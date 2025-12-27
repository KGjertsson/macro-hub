package com.kg.macroanalyzer.adaptors.web;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class RiksbankenAdaptor extends WebAdaptor {

    @Value("${riskbanken.prod.subscription.key}")
    private String subscriptionKey;

    @Override
    protected HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws IOException, URISyntaxException {
        final var endpointUrl = seriesConfig.scrapeUrl();
        final var url = new URI(endpointUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (subscriptionKey != null) {
            // Add custom header "Ocp-Apim-Subscription-Key" with its value
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        }

        return connection;
    }

    @Override
    protected Stream<MacroPoint> parseResponse(String response) throws ScrapeException {
        try {
            final var objectMapper = createObjectMapper();
            JavaType collectionType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ScrapedResponse.class);
            final List<ScrapedResponse> resultList = objectMapper.readValue(response, collectionType);

            return resultList.stream()
                    .map(ScrapedResponse::toMacroPoint);
        } catch (JsonProcessingException jsonProcessingException) {
            final var msgRaw = "Caught exception while parsing response from Riskbanken's API: %s";
            final var msgFormatted = String.format(msgRaw, jsonProcessingException.getMessage());
            log.error(msgFormatted);

            throw new ScrapeException(jsonProcessingException.getMessage());
        }
    }

    private ObjectMapper createObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
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
