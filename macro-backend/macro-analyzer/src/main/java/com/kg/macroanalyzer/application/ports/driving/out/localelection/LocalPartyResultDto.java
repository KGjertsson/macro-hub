package com.kg.macroanalyzer.application.ports.driving.out.localelection;

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
