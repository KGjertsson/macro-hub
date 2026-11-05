package com.kg.macroanalyzer.election.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder
public record LocalElectionDetailDto(
        LocalElectionSummaryDto summary,
        List<ElectedPartyDto> elected,
        GovernanceDto governance
) {

    public LocalElectionDetailDto {
        elected = ImmutableCopies.list(elected);
    }

}
