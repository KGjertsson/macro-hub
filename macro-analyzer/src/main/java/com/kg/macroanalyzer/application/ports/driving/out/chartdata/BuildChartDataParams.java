package com.kg.macroanalyzer.application.ports.driving.out.chartdata;

import com.kg.macroanalyzer.application.services.bundleformatservice.samplestrategy.StrategyFactory;
import lombok.Builder;

import java.util.List;

@Builder
public record BuildChartDataParams(
        StrategyFactory.Strategy strategy,
        List<ChartSeriesParam> chartSeriesParams
) {
}
