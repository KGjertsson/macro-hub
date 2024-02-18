package com.kg.macroanalyzer.application.ports.driving;

import lombok.Builder;

import java.time.temporal.Temporal;
import java.util.List;

@Builder
public record ChartDataWithLabels(List<Temporal> labels, List<ChartData> chartData) {
}
