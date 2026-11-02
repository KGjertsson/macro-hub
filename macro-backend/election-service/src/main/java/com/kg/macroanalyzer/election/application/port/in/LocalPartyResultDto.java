package com.kg.macroanalyzer.election.application.port.in;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LocalPartyResultDto(
        String code,
        String abbreviation,
        String name,
        String color,
        int votes,
        BigDecimal voteShare,
        int seats
) {
}
