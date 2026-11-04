package com.kg.macroanalyzer.regionalstatistics.application.port.in;

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
}
