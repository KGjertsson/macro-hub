package com.kg.macroanalyzer.regionalstatistics.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// A null value means that at least one summed cell was missing. The maps are wrapped rather than copied, because a
// DeSO response holds thousands of areas and every SCB data chunk builds one of these
public record ScbTableValues(
        Map<String, Map<Integer, Double>> values
) {

    public ScbTableValues {
        if (values != null) {
            final var wrapped = new HashMap<String, Map<Integer, Double>>(values.size() * 2);
            values.forEach((region, byYear) -> wrapped.put(region, Collections.unmodifiableMap(byYear)));
            values = Collections.unmodifiableMap(wrapped);
        }
    }

}
