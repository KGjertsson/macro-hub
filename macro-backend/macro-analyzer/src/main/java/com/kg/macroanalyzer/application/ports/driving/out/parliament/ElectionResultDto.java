package com.kg.macroanalyzer.application.ports.driving.out.parliament;

import lombok.Builder;

import java.util.List;

@Builder
public record ElectionResultDto(int year, String termStart, String termEnd, List<PartySeats> parties) {
}
