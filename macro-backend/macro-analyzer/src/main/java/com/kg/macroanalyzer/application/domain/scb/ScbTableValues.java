package com.kg.macroanalyzer.application.domain.scb;

import java.util.Map;

// Values per region code and year, summed over the selected codes of every other dimension.
// A null value means that at least one summed cell was missing
public record ScbTableValues(
        Map<String, Map<Integer, Double>> values
) {
}
