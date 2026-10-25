package com.kg.macroanalyzer.application.domain.localelection;

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
