package com.kg.macroanalyzer.regionalstatistics.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
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

    public ScbIndicatorResult {
        tableStamps = ImmutableCopies.map(tableStamps);
        values = ImmutableCopies.list(values);
    }

}
