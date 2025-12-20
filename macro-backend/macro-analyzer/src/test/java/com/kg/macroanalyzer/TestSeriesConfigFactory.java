package com.kg.macroanalyzer;

import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.util.List;

public class TestSeriesConfigFactory {

    public static List<SeriesConfig> buildTestConfig() {
        return List.of(
                SeriesConfig.builder()
                        .name("macro_series_1")
                        .displayName("Name 1")
                        .scrapeUrl("url1")
                        .build(),
                SeriesConfig.builder()
                        .name("macro_series_2")
                        .displayName("Name 2")
                        .scrapeUrl("url2")
                        .build(),
                SeriesConfig.builder()
                        .name("macro_series_3")
                        .displayName("Name 3")
                        .scrapeUrl("url3")
                        .build()
        );
    }

}
