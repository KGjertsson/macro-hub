package com.kg.macroanalyzer.application.domain.macroseries;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder(toBuilder = true)
public record MacroSeries(@NonNull String name, @NonNull List<MacroPoint> macroPoints) {

    public List<Double> getValues() {
        return this.macroPoints.stream()
                .map(MacroPoint::value)
                .toList();
    }

}
