package com.kg.macroanalyzer.regionalstatistics.domain;

public record ScbAvailability(
        String indicatorId,
        GeoVersion geoVersion,
        int year
) {
}
