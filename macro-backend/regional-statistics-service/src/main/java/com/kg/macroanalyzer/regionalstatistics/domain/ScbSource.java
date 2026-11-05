package com.kg.macroanalyzer.regionalstatistics.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;
import java.util.Map;

public record ScbSource(
        String table,
        Map<String, List<String>> selection
) {

    public ScbSource {
        selection = ImmutableCopies.mapOfLists(selection);
    }

    public long cellsPerAreaAndYear() {
        return selection.values().stream()
                .mapToLong(List::size)
                .reduce(1, (a, b) -> a * b);
    }

}
