package com.kg.macroanalyzer.parliament.application.port.in;

import lombok.Builder;

import java.util.List;

@Builder
public record CompositionDto(String asOf, List<PartySeats> parties) {
}
