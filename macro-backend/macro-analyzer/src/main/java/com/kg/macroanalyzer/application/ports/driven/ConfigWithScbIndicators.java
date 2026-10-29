package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicatorResult;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
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
        List<ScbIndicatorResult> results
) {
}
