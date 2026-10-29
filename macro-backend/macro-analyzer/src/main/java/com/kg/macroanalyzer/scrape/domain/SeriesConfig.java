package com.kg.macroanalyzer.scrape.domain;

import lombok.Builder;

@Builder
public record SeriesConfig(
        String name,
        String displayName,
        String scrapeUrl,
        String category,
        String unit,
        String subcategory,
        String country,
        Integer maturityMonths
) {
}
