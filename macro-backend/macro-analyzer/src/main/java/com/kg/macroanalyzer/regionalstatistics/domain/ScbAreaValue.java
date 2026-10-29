package com.kg.macroanalyzer.regionalstatistics.domain;

public record ScbAreaValue(
        int year,
        GeoVersion geoVersion,
        String areaCode,
        double value
) {
}
