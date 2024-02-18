package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.domain.MacroPoint;

import java.util.List;

public record ChartData(List<MacroPoint> data, String name, String color) {
}
