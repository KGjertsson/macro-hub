package com.kg.macroanalyzer.election.application.port.out;

import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
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
