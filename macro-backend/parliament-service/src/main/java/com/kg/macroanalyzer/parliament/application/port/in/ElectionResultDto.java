package com.kg.macroanalyzer.parliament.application.port.in;

import lombok.Builder;

import java.util.List;

@Builder
public record ElectionResultDto(int year, String termStart, String termEnd, List<PartySeats> parties) {
}
