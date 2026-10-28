package com.kg.macroanalyzer.application.domain.macroseries;


import lombok.Builder;
import lombok.NonNull;

import java.time.temporal.Temporal;
import java.util.List;

// Every series has one point per label, except series without data in the time frame, which are empty
@Builder
public record AlignedBundle(
        @NonNull List<MacroSeries> macroSeries,
        @NonNull List<Temporal> labels
) {
}
