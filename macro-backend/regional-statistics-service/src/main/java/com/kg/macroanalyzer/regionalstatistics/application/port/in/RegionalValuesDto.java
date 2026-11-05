package com.kg.macroanalyzer.regionalstatistics.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.Map;

@Builder
public record RegionalValuesDto(
        String indicator,
        String level,
        int year,
        String geoVersion,
        Map<String, Double> values
) {

    public RegionalValuesDto {
        values = ImmutableCopies.map(values);
    }

}
