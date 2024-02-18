package com.kg.macroanalyzer.application.ports.driving;

import lombok.Builder;

import java.util.List;

@Builder
public record ChartData(List<Double> values, String name, String color) {
}
