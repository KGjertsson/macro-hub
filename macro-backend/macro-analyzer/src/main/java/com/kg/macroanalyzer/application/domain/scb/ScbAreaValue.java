package com.kg.macroanalyzer.application.domain.scb;

public record ScbAreaValue(
        int year,
        GeoVersion geoVersion,
        String areaCode,
        double value
) {
}
