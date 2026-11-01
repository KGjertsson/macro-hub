package com.kg.macroanalyzer.regionalstatistics.domain;

// The values of one area division version for one year
public record ScbSlice(
        int year,
        GeoVersion geoVersion
) {
}
