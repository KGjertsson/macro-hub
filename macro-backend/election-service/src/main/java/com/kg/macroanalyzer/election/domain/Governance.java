package com.kg.macroanalyzer.election.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder
public record Governance(
        int year,
        LocalElectionType type,
        String areaCode,
        String coalitionName,
        String description,
        List<String> governingParties,
        List<String> supportParties
) {

    public Governance {
        governingParties = ImmutableCopies.list(governingParties);
        supportParties = ImmutableCopies.list(supportParties);
    }

}
