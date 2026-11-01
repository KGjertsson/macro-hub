package com.kg.macroanalyzer.macroseries.domain;


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
