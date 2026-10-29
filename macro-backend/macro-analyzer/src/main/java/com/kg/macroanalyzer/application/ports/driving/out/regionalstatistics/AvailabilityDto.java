package com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics;

import lombok.Builder;

@Builder
public record AvailabilityDto(
        int year,
        String geoVersion
) {
}
