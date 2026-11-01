package com.kg.macroanalyzer.macroseries.adaptors.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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

            if (timeLabels.isEmpty() || dataset.value == null) {
                return Stream.empty();
            }

            return dataset.value.entrySet().stream()
                    .map(entry ->
                            MacroPoint.builder()
                                    .value(entry.getValue())
                                    .date(timeLabels.get(Integer.parseInt(entry.getKey())))
                                    .build()
                    );
        } catch (JacksonException e) {
            throw new ScrapeException("Failed to parse Eurostat JSON response: " + e.getMessage());
        }
    }

    private ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
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
