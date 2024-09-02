package com.kg.macroanalyzer.application.ports.driving.out.chartdata;

import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.StrategyFactory;
import com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy.TimeFilterStrategyFactory;
import lombok.Builder;

import java.util.List;

@Builder
public record BuildChartDataParams(
        StrategyFactory.Strategy strategy,
        TimeFilterStrategyFactory.TimeFrame timeFrame,
        List<ChartSeriesParam> chartSeriesParams
) {
}
