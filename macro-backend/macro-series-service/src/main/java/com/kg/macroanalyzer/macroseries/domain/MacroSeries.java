package com.kg.macroanalyzer.macroseries.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder(toBuilder = true)
public record MacroSeries(@NonNull String name, @NonNull List<MacroPoint> macroPoints) {

    public MacroSeries {
        macroPoints = ImmutableCopies.list(macroPoints);
    }

    public List<Double> getValues() {
        return this.macroPoints.stream()
                .map(MacroPoint::value)
                .toList();
    }

}
