package com.kg.macroanalyzer.application.domain;


import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDate;

@Builder
public record MacroPoint(@NonNull Double value, @NonNull LocalDate date) {
}
