package com.kg.macroanalyzer.election.domain;

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
}
