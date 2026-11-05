package com.kg.macroanalyzer.macroseries.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;
import lombok.NonNull;

import java.time.temporal.Temporal;
import java.util.List;

@Builder
public record AlignedBundle(
        @NonNull List<MacroSeries> macroSeries,
        @NonNull List<Temporal> labels
) {

    public AlignedBundle {
        macroSeries = ImmutableCopies.list(macroSeries);
        labels = ImmutableCopies.list(labels);
    }

}
