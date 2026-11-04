package com.kg.macroanalyzer.regionalstatistics.domain;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record ScbIndicatorResult(
        ScbIndicator indicator,
        Map<String, Instant> tableStamps,
        List<ScbAreaValue> values
) {
}
