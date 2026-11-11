package com.kg.macroanalyzer.parliament.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record Election(
        int year,
        LocalDate termStart,
        LocalDate termEnd,
        Map<String, Integer> seatsByParty
) {

    public Election {
        seatsByParty = ImmutableCopies.map(seatsByParty);
    }

}
