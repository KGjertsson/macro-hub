package com.kg.macroanalyzer.macroseries.application.port.in;

import lombok.Builder;

import java.time.temporal.Temporal;
import java.util.List;

@Builder
public record ChartDataWithLabels(List<Temporal> labels, List<ChartData> chartData) {
}
