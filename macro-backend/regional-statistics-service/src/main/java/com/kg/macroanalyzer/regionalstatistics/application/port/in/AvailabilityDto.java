package com.kg.macroanalyzer.regionalstatistics.application.port.in;

import lombok.Builder;

@Builder
public record AvailabilityDto(
        int year,
        String geoVersion
) {
}
