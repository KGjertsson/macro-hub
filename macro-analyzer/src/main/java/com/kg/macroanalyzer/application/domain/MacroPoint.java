package com.kg.macroanalyzer.application.domain;


import lombok.Builder;
import lombok.NonNull;

import java.time.temporal.Temporal;

@Builder(toBuilder = true)
public record MacroPoint(@NonNull Double value, @NonNull Temporal date) {

}
