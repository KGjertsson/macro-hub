package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.SampleStrategy.MacroSamplerStrategyFactory;
import com.kg.macroanalyzer.application.domain.MacroBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BundleFormatter {

    private final LabelGenerator labelGenerator;
    private final MacroSamplerStrategyFactory macroSamplerStrategyFactory;

    @Autowired
    public BundleFormatter(
            LabelGenerator labelGenerator,
            MacroSamplerStrategyFactory macroSamplerStrategyFactory
    ) {
        this.labelGenerator = labelGenerator;
        this.macroSamplerStrategyFactory = macroSamplerStrategyFactory;
    }

    public Optional<MacroBundle> align(MacroBundle macroBundleRaw) {
        final var sampleStrategy = macroSamplerStrategyFactory.build();

        return Optional.ofNullable(macroBundleRaw)
                .flatMap(labelGenerator::padToFullLabels)
                .flatMap(sampleStrategy::sample);
    }

}
