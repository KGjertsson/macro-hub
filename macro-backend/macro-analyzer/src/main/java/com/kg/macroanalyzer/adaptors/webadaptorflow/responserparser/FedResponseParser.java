package com.kg.macroanalyzer.adaptors.webadaptorflow.responserparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public record FedResponseParser() implements ResponseParser<String, Stream<MacroPoint>> {

    @Override
    public Stream<MacroPoint> parse(String response) throws ScrapeException {
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
