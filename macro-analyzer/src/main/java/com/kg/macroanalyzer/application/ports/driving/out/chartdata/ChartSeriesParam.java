package com.kg.macroanalyzer.application.ports.driving.out.chartdata;

import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

public record ChartSeriesParam(String name, String country, String period) {

    public static ChartSeriesParam ofSeriesConfig(SeriesConfig seriesConfig) {
        return new ChartSeriesParam(
                seriesConfig.name(),
                seriesConfig.country(),
                seriesConfig.period()
        );
    }

}
