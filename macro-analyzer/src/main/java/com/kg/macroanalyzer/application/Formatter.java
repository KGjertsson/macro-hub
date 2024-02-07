package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Formatter {

    private final LabelGenerator labelGenerator;
    private final MacroSamplerStrategy macroSamplerStrategy;

    @Autowired
    public Formatter(LabelGenerator labelGenerator, MacroSamplerStrategy macroSamplerStrategy) {
        this.labelGenerator = labelGenerator;
        this.macroSamplerStrategy = macroSamplerStrategy;
    }

    public Optional<MacroBundle> align(MacroBundle macroBundleRaw) {
        return Optional.ofNullable(macroBundleRaw)
                .flatMap(labelGenerator::padToFullLabels)
                .flatMap(macroSamplerStrategy::sample);
    }

}
