package com.kg.macroanalyzer.regionalstatistics.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.Map;

// A null value means that at least one summed cell was missing
public record ScbTableValues(
        Map<String, Map<Integer, Double>> values
) {

    public ScbTableValues {
        values = ImmutableCopies.mapOfMaps(values);
    }

}
