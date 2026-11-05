package com.kg.macroanalyzer.macroseries.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.time.temporal.Temporal;
import java.util.List;

@Builder
public record ChartDataWithLabels(List<Temporal> labels, List<ChartData> chartData) {

    public ChartDataWithLabels {
        labels = ImmutableCopies.list(labels);
        chartData = ImmutableCopies.list(chartData);
    }

}
