package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record EuroStatResponseParser() implements ResponseParser<String, Stream<MacroPoint>> {

    @Override
    public Stream<MacroPoint> parse(String response) throws ScrapeException {
        try {
            final var mapper = createObjectMapper();
            final var dataset = mapper.readValue(response, EurostatDataset.class);
            final var timeLabels = dataset.orderedTimeLabels();

            return dataset.value.entrySet().stream()
                    .map(entry ->
                            MacroPoint.builder()
                                    .value(entry.getValue())
                                    .date(timeLabels.get(Integer.parseInt(entry.getKey())))
                                    .build()
                    );
        } catch (JsonProcessingException e) {
            throw new ScrapeException("Failed to parse Eurostat JSON response: " + e.getMessage());
        }
    }

    private ObjectMapper createObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public record EurostatDataset(
            String version,
            @JsonProperty("class") String clazz,
            String label,
            String source,
            String updated,
            Map<String, Double> value,
            List<String> id,
            List<Integer> size,
            Dimension dimension,
            Map<String, Object> extension
    ) {

        public List<LocalDate> orderedTimeLabels() {
            if (dimension == null || dimension.time == null || dimension.time.category == null)
                return List.of();
            final var idx = dimension.time.category.index;
            if (idx == null || idx.isEmpty()) return List.of();
            final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            return idx.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .map(year -> year + "-01-01")
                    .map(date -> LocalDate.parse(date, formatter))
                    .toList();
        }
    }

    public record Dimension(
            Freq freq,
            Unit unit,
            Sector sector,
            @JsonProperty("na_item") NaItem naItem,
            Geo geo,
            Time time
    ) {
    }

    public record Freq(String label, Category category) {
    }

    public record Unit(String label, Category category) {
    }

    public record Sector(String label, Category category) {
    }

    public record NaItem(String label, Category category) {
    }

    public record Geo(String label, Category category) {
    }

    public record Time(String label, Category category) {
    }

    public record Category(
            Map<String, Integer> index,
            Map<String, String> label
    ) {
    }

}
