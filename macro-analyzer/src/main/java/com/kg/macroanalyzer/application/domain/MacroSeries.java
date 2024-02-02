package com.kg.macroanalyzer.application.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record MacroSeries(String name, List<MacroPoint> macroPoints) {
}
