package com.kg.macroanalyzer.macroseries.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder
public record BuildChartDataParams(
        Strategy strategy,
        TimeFrame timeFrame,
        List<ChartSeriesParam> chartSeriesParams
) {

    public BuildChartDataParams {
        chartSeriesParams = ImmutableCopies.list(chartSeriesParams);
    }

}
