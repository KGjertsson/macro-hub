package com.kg.macroanalyzer.application.ports.driving.seriesconfig;

import lombok.Builder;

@Builder
public record SeriesConfig(String name, String displayName, String country, String period) {
}
