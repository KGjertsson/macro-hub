package com.kg.macroanalyzer.application.ports.driving.out.localelection;

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
