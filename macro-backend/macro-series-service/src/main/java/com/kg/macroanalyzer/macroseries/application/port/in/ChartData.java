package com.kg.macroanalyzer.macroseries.application.port.in;

import lombok.Builder;

import java.util.List;

@Builder
public record ChartData(List<Double> values, String name, String color) {
}
