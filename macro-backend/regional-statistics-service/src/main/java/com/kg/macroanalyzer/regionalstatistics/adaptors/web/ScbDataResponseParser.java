package com.kg.macroanalyzer.regionalstatistics.adaptors.web;

import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableMetadata;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableValues;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Sums a JSON-stat2 data response per region and year over every other dimension. The values are stored
// row-major over the dimensions in "id" order, so the last dimension changes fastest. A missing cell makes the
// whole sum null, because a partial sum would understate the area
public record ScbDataResponseParser(String table) implements ResponseParser<String, ScbTableValues> {

    @Override
    public ScbTableValues parse(String response) throws ScrapeException {
        final var root = JsonStat2.readTree(response, table);
        final var codes = List.copyOf(JsonStat2.codes(root).entrySet());
        final var regionDimension = dimensionPosition(codes, ScbTableMetadata.REGION);
        final var timeDimension = dimensionPosition(codes, ScbTableMetadata.TIME);
        final var years = years(codes.get(timeDimension).getValue());
        final var sizes = codes.stream().mapToInt(e -> e.getValue().size()).toArray();
        final var cellCount = Arrays.stream(sizes).asLongStream().reduce(1, (a, b) -> a * b);
        final var value = root.path("value");

        final var sums = new HashMap<String, Map<Integer, Double>>();
        final var coordinates = new int[sizes.length];
        for (long cell = 0; cell < cellCount; cell++) {
            var rest = cell;
            for (int d = sizes.length - 1; d >= 0; d--) {
                coordinates[d] = (int) (rest % sizes[d]);
                rest /= sizes[d];
            }
            final var region = codes.get(regionDimension).getValue().get(coordinates[regionDimension]);
            final var year = years.get(coordinates[timeDimension]);
            final var cellValue = cellValue(value, cell);
            final var byYear = sums.computeIfAbsent(region, r -> new HashMap<>());
            if (!byYear.containsKey(year)) {
                byYear.put(year, cellValue);
            } else {
                final var sum = byYear.get(year);
                byYear.put(year, sum == null || cellValue == null ? null : sum + cellValue);
            }
        }

        return new ScbTableValues(sums);
    }

    private int dimensionPosition(List<Map.Entry<String, List<String>>> codes, String dimension) throws ScrapeException {
        for (int i = 0; i < codes.size(); i++) {
            if (codes.get(i).getKey().equals(dimension)) {
                return i;
            }
        }

        throw new ScrapeException("No %s dimension in SCB data for %s".formatted(dimension, table));
    }

    // value is an array with nulls, or an object from cell index to value when the response is sparse
    private static Double cellValue(JsonNode value, long cell) {
        final var node = value.isArray() ? value.get((int) cell) : value.get(Long.toString(cell));

        return node != null && node.isNumber() ? node.asDouble() : null;
    }

    // Only yearly tables are used, so every Tid code is a year
    private List<Integer> years(List<String> codes) throws ScrapeException {
        final var years = new ArrayList<Integer>();
        for (final var code : codes) {
            try {
                years.add(Integer.valueOf(code));
            } catch (NumberFormatException e) {
                throw new ScrapeException("Unexpected period %s in SCB data for %s".formatted(code, table));
            }
        }

        return years;
    }

}
