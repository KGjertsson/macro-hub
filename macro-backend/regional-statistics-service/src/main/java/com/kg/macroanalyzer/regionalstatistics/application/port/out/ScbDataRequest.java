package com.kg.macroanalyzer.regionalstatistics.application.port.out;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;
import java.util.Map;

public record ScbDataRequest(
        String baseUrl,
        String table,
        String regionCodes,
        List<String> years,
        Map<String, List<String>> selection
) {

    public ScbDataRequest {
        years = ImmutableCopies.list(years);
        selection = ImmutableCopies.mapOfLists(selection);
    }

}
