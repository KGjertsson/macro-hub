package com.kg.macroanalyzer.election.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder
public record GovernanceDto(
        String coalitionName,
        String description,
        List<String> governingParties,
        List<String> supportParties
) {

    public GovernanceDto {
        governingParties = ImmutableCopies.list(governingParties);
        supportParties = ImmutableCopies.list(supportParties);
    }

}
