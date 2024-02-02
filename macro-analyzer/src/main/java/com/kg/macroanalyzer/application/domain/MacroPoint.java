package com.kg.macroanalyzer.application.domain;


import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MacroPoint(Double value, LocalDate date) {
}
