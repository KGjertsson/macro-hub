package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.samplestrategy.StrategyFactory;

import java.util.List;

public record BuildChartDataParams(
        StrategyFactory.Strategy strategy,
        List<ChartSeriesParam> chartSeriesParams
) {
}
