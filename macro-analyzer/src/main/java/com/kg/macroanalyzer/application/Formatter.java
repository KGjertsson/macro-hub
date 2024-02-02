package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

public class Formatter {

    private final LabelGenerator labelGenerator;
    private final MacroSampler macroSampler;

    public Formatter(LabelGenerator labelGenerator, MacroSampler macroSampler) {
        this.labelGenerator = labelGenerator;
        this.macroSampler = macroSampler;
    }

    public Optional<MacroBundle> align(MacroBundle macroBundleRaw) {
        return Optional.ofNullable(macroBundleRaw)
                .map(labelGenerator::generateDays)
                .map(macroSampler::sample);
    }

    @Builder
    public record WithFullLabels(MacroBundle macroBundle, List<String> labels) {
    }

}
