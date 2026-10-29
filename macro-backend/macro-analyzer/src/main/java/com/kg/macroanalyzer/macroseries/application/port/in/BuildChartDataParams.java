package com.kg.macroanalyzer.macroseries.application.port.in;

import com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy.StrategyFactory;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.timefilterstrategy.TimeFilterStrategyFactory;
import lombok.Builder;

import java.util.List;

@Builder
public record BuildChartDataParams(
        StrategyFactory.Strategy strategy,
        TimeFilterStrategyFactory.TimeFrame timeFrame,
        List<ChartSeriesParam> chartSeriesParams
) {
}
