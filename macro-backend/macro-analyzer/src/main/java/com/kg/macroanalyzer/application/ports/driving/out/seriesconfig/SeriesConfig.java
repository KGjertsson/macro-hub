package com.kg.macroanalyzer.application.ports.driving.out.seriesconfig;

import lombok.Builder;

@Builder
public record SeriesConfig(
        String name,
        String displayName,
        String scrapeUrl,
        String category
) {
}
