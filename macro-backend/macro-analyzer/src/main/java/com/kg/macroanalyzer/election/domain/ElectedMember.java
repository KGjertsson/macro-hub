package com.kg.macroanalyzer.election.domain;

import lombok.Builder;

@Builder
public record ElectedMember(
        String name,
        String partyCode,
        String partyAbbreviation,
        int order,
        String electedOn
) {
}
