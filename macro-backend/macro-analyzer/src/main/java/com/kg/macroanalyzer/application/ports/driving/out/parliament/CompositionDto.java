package com.kg.macroanalyzer.application.ports.driving.out.parliament;

import lombok.Builder;

import java.util.List;

@Builder
public record CompositionDto(String asOf, List<PartySeats> parties) {
}
