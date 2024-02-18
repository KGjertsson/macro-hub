package com.kg.macroanalyzer.application.ports.driving;

import java.time.temporal.Temporal;
import java.util.List;

public record ChartDataWithLabels(List<Temporal> labels, List<ChartData> chartData) {
}
