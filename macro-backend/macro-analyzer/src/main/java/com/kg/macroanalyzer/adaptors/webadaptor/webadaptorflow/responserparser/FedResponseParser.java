package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

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
        } catch (JacksonException jsonProcessingException) {
            final var msgRaw = "Caught exception while parsing response from FED's API: %s";
            final var msgFormatted = String.format(msgRaw, jsonProcessingException.getMessage());
            log.error(msgFormatted);

            throw new ScrapeException(jsonProcessingException.getMessage());
        }
    }


    private ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
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
