package com.kg.macroanalyzer.application.ports.driving.out.localelection;

import lombok.Builder;

import java.util.List;

@Builder
public record ElectedPartyDto(
        String partyCode,
        String abbreviation,
        String name,
        String color,
        List<ElectedMemberDto> members
) {
}
