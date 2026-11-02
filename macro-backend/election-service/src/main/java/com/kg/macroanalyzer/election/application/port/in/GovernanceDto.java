package com.kg.macroanalyzer.election.application.port.in;

import lombok.Builder;

import java.util.List;

@Builder
public record GovernanceDto(
        String coalitionName,
        String description,
        List<String> governingParties,
        List<String> supportParties
) {
}
