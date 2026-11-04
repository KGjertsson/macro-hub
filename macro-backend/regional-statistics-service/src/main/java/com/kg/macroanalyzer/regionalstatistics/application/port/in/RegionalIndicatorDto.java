package com.kg.macroanalyzer.regionalstatistics.application.port.in;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record RegionalIndicatorDto(
        String id,
        String label,
        String description,
        String category,
        String unit,
        int decimals,
        Map<String, List<AvailabilityDto>> availability
) {
}
