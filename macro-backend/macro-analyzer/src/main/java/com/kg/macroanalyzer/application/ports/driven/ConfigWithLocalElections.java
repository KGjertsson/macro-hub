package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder(toBuilder = true)
public record ConfigWithLocalElections(
        SeriesConfig seriesConfig,
        LocalElectionType type,
        Set<String> existingMd5s,
        List<LocalElectionResult> results
) {
}
