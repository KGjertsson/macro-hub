package com.kg.macroanalyzer.application.domain;


import lombok.Builder;
import lombok.NonNull;

import java.time.temporal.Temporal;
import java.util.List;

@Builder
public record AlignedBundle(
        @NonNull List<MacroSeries> macroSeries,
        @NonNull List<Temporal> labels
) {
}
