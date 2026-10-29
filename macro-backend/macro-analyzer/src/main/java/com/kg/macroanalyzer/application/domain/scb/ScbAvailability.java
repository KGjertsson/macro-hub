package com.kg.macroanalyzer.application.domain.scb;

public record ScbAvailability(
        String indicatorId,
        GeoVersion geoVersion,
        int year
) {
}
