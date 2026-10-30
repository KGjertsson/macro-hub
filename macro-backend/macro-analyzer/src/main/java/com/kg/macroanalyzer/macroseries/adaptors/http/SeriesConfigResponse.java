package com.kg.macroanalyzer.macroseries.adaptors.http;

import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

// A chart series in /series-config. Leaves out scrapeFlow, which only routes scrapes
public record SeriesConfigResponse(
        String name,
        String displayName,
        String scrapeUrl,
        String category,
        String unit,
        String subcategory,
        String country,
        Integer maturityMonths
) {

    public static SeriesConfigResponse from(SeriesConfig config) {
        return new SeriesConfigResponse(
                config.name(),
                config.displayName(),
                config.scrapeUrl(),
                config.category(),
                config.unit(),
                config.subcategory(),
                config.country(),
                config.maturityMonths()
        );
    }

}
