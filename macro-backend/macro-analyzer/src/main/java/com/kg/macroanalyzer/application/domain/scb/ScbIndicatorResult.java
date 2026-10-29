package com.kg.macroanalyzer.application.domain.scb;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// Every value of one indicator, plus the SCB "updated" stamps of the tables they were fetched from
@Builder(toBuilder = true)
public record ScbIndicatorResult(
        ScbIndicator indicator,
        Map<String, Instant> tableStamps,
        List<ScbAreaValue> values
) {
}
