package com.kg.macroanalyzer.regionalstatistics.application.port.out;

import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicator;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicatorResult;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder(toBuilder = true)
public record ConfigWithScbIndicators(
        SeriesConfig seriesConfig,
        List<ScbIndicator> indicators,
        Map<String, Instant> tableStamps,
        Set<String> indicatorsWithData,
        List<ScbIndicatorResult> results,
        // Tables whose fetch failed in this run; their stored stamps are dropped so dependent indicators are re-fetched
        Set<String> failedTables
) {

    public ConfigWithScbIndicators {
        indicators = ImmutableCopies.list(indicators);
        tableStamps = ImmutableCopies.map(tableStamps);
        indicatorsWithData = ImmutableCopies.set(indicatorsWithData);
        results = ImmutableCopies.list(results);
        failedTables = ImmutableCopies.set(failedTables);
    }

}
