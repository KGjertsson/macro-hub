package com.kg.macroanalyzer.election.application.port.in;

import lombok.Builder;

import java.util.List;

@Builder
public record LocalElectionDetailDto(
        LocalElectionSummaryDto summary,
        List<ElectedPartyDto> elected,
        GovernanceDto governance
) {
}
