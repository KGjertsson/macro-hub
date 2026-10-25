package com.kg.macroanalyzer.application.domain.localelection;

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
