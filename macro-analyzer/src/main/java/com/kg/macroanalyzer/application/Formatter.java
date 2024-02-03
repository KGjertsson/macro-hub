package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class Formatter {

    private final LabelGenerator labelGenerator;
    private final MacroSampler macroSampler;

    @Autowired
    public Formatter(LabelGenerator labelGenerator, MacroSampler macroSampler) {
        this.labelGenerator = labelGenerator;
        this.macroSampler = macroSampler;
    }

    public Optional<MacroBundle> align(MacroBundle macroBundleRaw) {
        return Optional.ofNullable(macroBundleRaw)
                .flatMap(labelGenerator::generateFullLabels)
                .map(macroSampler::sample);
    }

    @Builder
    public record WithFullLabels(MacroBundle macroBundle, List<LocalDate> labels) {
    }

}
