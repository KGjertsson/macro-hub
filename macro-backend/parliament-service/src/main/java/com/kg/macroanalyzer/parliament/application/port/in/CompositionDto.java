package com.kg.macroanalyzer.parliament.application.port.in;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder
public record CompositionDto(String asOf, List<PartySeats> parties) {

    public CompositionDto {
        parties = ImmutableCopies.list(parties);
    }

}
