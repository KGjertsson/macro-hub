package com.kg.macroanalyzer.parliament.domain;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record Election(int year, LocalDate termStart, LocalDate termEnd, Map<String, Integer> seatsByParty) {
}
