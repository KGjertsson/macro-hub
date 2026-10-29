package com.kg.macroanalyzer.application.domain.scb;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// codes holds every code per dimension in the table's order
public record ScbTableMetadata(
        String table,
        Instant updated,
        Map<String, List<String>> codes
) {

    public static final String REGION = "Region";
    public static final String TIME = "Tid";

    public List<String> regionCodes() {
        return codes.getOrDefault(REGION, List.of());
    }

    public List<String> years() {
        return codes.getOrDefault(TIME, List.of());
    }

}
