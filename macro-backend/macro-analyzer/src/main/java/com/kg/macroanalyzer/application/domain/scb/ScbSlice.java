package com.kg.macroanalyzer.application.domain.scb;

// The values of one area division version for one year
public record ScbSlice(
        int year,
        GeoVersion geoVersion
) {
}
