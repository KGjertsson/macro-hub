package com.kg.macroanalyzer.application.ports.driven;

import java.util.List;
import java.util.Map;

// regionCodes is a PxWeb valueCodes pattern (e.g. "*_DeSO2025") that selects one area division version
public record ScbDataRequest(
        String baseUrl,
        String table,
        String regionCodes,
        List<String> years,
        Map<String, List<String>> selection
) {
}
