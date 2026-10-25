package com.kg.macroanalyzer.application.ports.driving.out.localelection;

import lombok.Builder;

import java.util.List;

@Builder
public record LocalElectionDetailDto(
        LocalElectionSummaryDto summary,
        List<ElectedPartyDto> elected,
        GovernanceDto governance
) {
}
