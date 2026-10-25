package com.kg.macroanalyzer.application.ports.driving.out.localelection;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record LocalElectionSummaryDto(
        int year,
        String code,
        String name,
        String lanCode,
        int totalSeats,
        BigDecimal turnout,
        String largestParty,
        String largestPartyColor,
        List<LocalPartyResultDto> parties
) {
}
