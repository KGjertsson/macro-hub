package com.kg.macroanalyzer.election.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
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

    public ElectedPartyDto {
        members = ImmutableCopies.list(members);
    }

}
