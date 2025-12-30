package com.kg.macroanalyzer.adaptors.web.macroseries;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.adaptors.web.WebAdaptor;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
