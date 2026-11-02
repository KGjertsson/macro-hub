package com.kg.macroanalyzer.election.application.port.in;

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
