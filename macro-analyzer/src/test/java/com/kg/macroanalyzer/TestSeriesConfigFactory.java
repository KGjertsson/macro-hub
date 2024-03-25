package com.kg.macroanalyzer;

import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.util.List;

public class TestSeriesConfigFactory {

    public static List<SeriesConfig> buildTestConfig() {
        return List.of(
                SeriesConfig.builder()
                        .name("name1")
                        .displayName("Name 1")
                        .country("country1")
                        .period("MONTH")
                        .scrapeUrl("url1")
                        .build(),
                SeriesConfig.builder()
                        .name("name2")
                        .displayName("Name 2")
                        .country("country2")
                        .period("MONTH")
                        .scrapeUrl("url2")
                        .build()
        );
    }

}
