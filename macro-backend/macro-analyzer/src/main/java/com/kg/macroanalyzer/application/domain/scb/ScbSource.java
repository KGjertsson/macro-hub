package com.kg.macroanalyzer.application.domain.scb;

import java.util.List;
import java.util.Map;

// One SCB table and the codes to sum per dimension (ContentsCode included); Region and Tid are chosen by the scraper
public record ScbSource(
        String table,
        Map<String, List<String>> selection
) {

    public long cellsPerAreaAndYear() {
        return selection.values().stream()
                .mapToLong(List::size)
                .reduce(1, (a, b) -> a * b);
    }

}
