package com.kg.macroanalyzer.regionalstatistics.domain;

import lombok.Builder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

// A curated statistic. Without a denominator the value is the summed numerator times the multiplier
@Builder(toBuilder = true)
public record ScbIndicator(
        String id,
        String label,
        String description,
        String category,
        String unit,
        int decimals,
        List<ScbLevel> levels,
        List<ScbSource> numerator,
        List<ScbSource> denominator,
        double multiplier,
        int sortOrder
) {

    public Set<String> tables() {
        final var tables = new LinkedHashSet<String>();
        Stream.concat(numerator.stream(), denominator.stream())
                .map(ScbSource::table)
                .forEach(tables::add);

        return tables;
    }

    public List<GeoVersion> geoVersions() {
        return levels.stream()
                .flatMap(level -> level.versions().stream())
                .toList();
    }

}
