package com.kg.macroanalyzer.application.domain.parliament;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record Election(int year, LocalDate termStart, LocalDate termEnd, Map<String, Integer> seatsByParty) {
}
