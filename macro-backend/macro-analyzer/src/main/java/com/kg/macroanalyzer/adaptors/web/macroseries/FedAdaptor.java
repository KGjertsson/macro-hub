package com.kg.macroanalyzer.adaptors.web.macroseries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.adaptors.web.WebAdaptor;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
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
public class FedAdaptor extends WebAdaptor {

    @Value("${fed.prod.subscription.key}")
    private String subscriptionKey;

    @Override
    protected HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws IOException, URISyntaxException {
        final var endpointUrl = String.format(
                "%s&api_key=%s&file_type=json",
                seriesConfig.scrapeUrl(),
                subscriptionKey
        );
        final var url = new URI(endpointUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return connection;
    }

    @Override
    protected Stream<MacroPoint> parseResponse(String response) throws ScrapeException {
        try {
            final var objectMapper = createObjectMapper();
            JavaType javaType = objectMapper.constructType(ScrapedResponse.class);
            final ScrapedResponse scrapedResponse = objectMapper.readValue(response, javaType);

            return scrapedResponse.observations.stream()
                    .flatMap(Observation::toMacroPoint);
        } catch (JsonProcessingException jsonProcessingException) {
            final var msgRaw = "Caught exception while parsing response from FED's API: %s";
            final var msgFormatted = String.format(msgRaw, jsonProcessingException.getMessage());
            log.error(msgFormatted);

            throw new ScrapeException(jsonProcessingException.getMessage());
        }
    }


    private ObjectMapper createObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper;
    }

    private record ScrapedResponse(
            String realtimeStart,
            String realtimeEnd,
            String observationStart,
            String observationEnd,
            String units,
            String outputType,
            String fileType,
            String orderBy,
            String sortOrder,
            Integer count,
            Integer offset,
            Integer limit,
            List<Observation> observations
    ) {
    }

    private record Observation(
            String realtimeStart,
            String realtimeEnd,
            String date,
            String value
    ) {

        public Stream<MacroPoint> toMacroPoint() {
            try {
                final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                return Stream.of(
                        MacroPoint.builder()
                                .value(Double.valueOf(value))
                                .date(LocalDate.parse(date, formatter))
                                .build()
                );
            } catch (NumberFormatException numberFormatException) {
                return Stream.empty();
            }
        }

    }

}
