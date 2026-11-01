package com.kg.macroanalyzer.macroseries.application.port.in;

import lombok.Builder;

import java.util.List;

@Builder
public record BuildChartDataParams(
        Strategy strategy,
        TimeFrame timeFrame,
        List<ChartSeriesParam> chartSeriesParams
) {
}
