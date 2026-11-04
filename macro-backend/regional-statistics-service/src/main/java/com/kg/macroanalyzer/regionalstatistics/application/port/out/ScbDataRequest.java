package com.kg.macroanalyzer.regionalstatistics.application.port.out;

import java.util.List;
import java.util.Map;

public record ScbDataRequest(
        String baseUrl,
        String table,
        String regionCodes,
        List<String> years,
        Map<String, List<String>> selection
) {
}
