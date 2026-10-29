package com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics;

import lombok.Builder;

import java.util.Map;

// values maps area codes, without SCB's version suffix, to the indicator's value
@Builder
public record RegionalValuesDto(
        String indicator,
        String level,
        int year,
        String geoVersion,
        Map<String, Double> values
) {
}
