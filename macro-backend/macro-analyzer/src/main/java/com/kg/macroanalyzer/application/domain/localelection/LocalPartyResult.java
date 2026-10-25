package com.kg.macroanalyzer.application.domain.localelection;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LocalPartyResult(
        String code,
        String abbreviation,
        String name,
        String color,
        int votes,
        BigDecimal voteShare,
        int seats
) {
}
