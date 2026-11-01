package com.kg.macroanalyzer.regionalstatistics.application.port.in;

import lombok.Builder;

import java.util.List;
import java.util.Map;

// availability maps each level ("DeSO", "RegSO") to its years, newest first, with the boundary version per year
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
