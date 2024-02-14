package com.kg.macroanalyzer.application.domain;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder(toBuilder = true)
public record MacroSeries(@NonNull String name, @NonNull List<MacroPoint> macroPoints) {
}
