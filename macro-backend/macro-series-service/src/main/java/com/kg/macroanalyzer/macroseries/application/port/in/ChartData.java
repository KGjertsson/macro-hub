package com.kg.macroanalyzer.macroseries.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder
public record ChartData(List<Double> values, String name, String color) {

    public ChartData {
        values = ImmutableCopies.list(values);
    }

}
