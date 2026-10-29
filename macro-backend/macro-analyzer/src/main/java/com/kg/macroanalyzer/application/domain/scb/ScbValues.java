package com.kg.macroanalyzer.application.domain.scb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Turns summed SCB values into stored area values. SCB fills the years a version has no data for with 0 or null,
// so a version's year is only kept when some value in it is neither null nor 0. Within a kept year a null
// (suppressed) value is dropped and a real 0 is kept
public final class ScbValues {

    private static final double ROUNDING = 10_000d;

    private ScbValues() {
    }

    public static Map<ScbSlice, Map<String, Double>> slices(ScbTableValues tableValues) {
        final var raw = new HashMap<ScbSlice, Map<String, Double>>();
        tableValues.values().forEach((regionCode, byYear) -> GeoVersion.fromRegionCode(regionCode)
                .ifPresent(version -> {
                    final var areaCode = version.areaCode(regionCode);
                    byYear.forEach((year, value) -> raw
                            .computeIfAbsent(new ScbSlice(year, version), s -> new HashMap<>())
                            .put(areaCode, value));
                }));

        final var kept = new HashMap<ScbSlice, Map<String, Double>>();
        raw.forEach((slice, values) -> {
            final var hasData = values.values().stream().anyMatch(v -> v != null && v != 0d);
            if (hasData) {
                final var nonNull = new HashMap<String, Double>();
                values.forEach((area, value) -> {
                    if (value != null) {
                        nonNull.put(area, value);
                    }
                });
                kept.put(slice, nonNull);
            }
        });

        return kept;
    }

    public static Map<ScbSlice, Map<String, Double>> scale(Map<ScbSlice, Map<String, Double>> values, double multiplier) {
        final var scaled = new HashMap<ScbSlice, Map<String, Double>>();
        values.forEach((slice, byArea) -> {
            final var scaledByArea = new HashMap<String, Double>();
            byArea.forEach((area, value) -> scaledByArea.put(area, round(value * multiplier)));
            scaled.put(slice, scaledByArea);
        });

        return scaled;
    }

    // Numerator and denominator are matched on year, version and area; areas without a denominator, or with 0, are left out
    public static Map<ScbSlice, Map<String, Double>> ratio(
            Map<ScbSlice, Map<String, Double>> numerator,
            Map<ScbSlice, Map<String, Double>> denominator,
            double multiplier
    ) {
        final var ratios = new HashMap<ScbSlice, Map<String, Double>>();
        numerator.forEach((slice, byArea) -> {
            final var denominatorByArea = denominator.get(slice);
            if (denominatorByArea == null) {
                return;
            }
            final var ratioByArea = new HashMap<String, Double>();
            byArea.forEach((area, value) -> {
                final var divisor = denominatorByArea.get(area);
                if (divisor != null && divisor != 0d) {
                    ratioByArea.put(area, round(value / divisor * multiplier));
                }
            });
            if (!ratioByArea.isEmpty()) {
                ratios.put(slice, ratioByArea);
            }
        });

        return ratios;
    }

    public static List<ScbAreaValue> toAreaValues(Map<ScbSlice, Map<String, Double>> values) {
        final var areaValues = new ArrayList<ScbAreaValue>();
        values.forEach((slice, byArea) -> byArea.forEach((area, value) ->
                areaValues.add(new ScbAreaValue(slice.year(), slice.geoVersion(), area, value))));

        return areaValues;
    }

    // Splits the years so that one request stays within SCB's cell limit; a single year is always requested on its own
    public static List<List<String>> yearChunks(List<String> years, long cellsPerYear, long maxCells) {
        final var yearsPerChunk = (int) Math.max(1, maxCells / Math.max(1, cellsPerYear));
        final var chunks = new ArrayList<List<String>>();
        for (int i = 0; i < years.size(); i += yearsPerChunk) {
            chunks.add(List.copyOf(years.subList(i, Math.min(years.size(), i + yearsPerChunk))));
        }

        return chunks;
    }

    private static double round(double value) {
        return Math.round(value * ROUNDING) / ROUNDING;
    }

}
