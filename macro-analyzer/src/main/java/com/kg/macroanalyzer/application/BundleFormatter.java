package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.SampleStrategy.StrategyFactory;
import com.kg.macroanalyzer.application.domain.MacroBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BundleFormatter {

    private final LabelGenerator labelGenerator;
    private final StrategyFactory strategyFactory;

    @Autowired
    public BundleFormatter(
            LabelGenerator labelGenerator,
            StrategyFactory strategyFactory
    ) {
        this.labelGenerator = labelGenerator;
        this.strategyFactory = strategyFactory;
    }

    public Optional<MacroBundle> align(
            MacroBundle macroBundleRaw,
            StrategyFactory.Strategy strategy
    ) {
        final var sampleStrategy = strategyFactory.build(strategy);

        return Optional.ofNullable(macroBundleRaw)
                .flatMap(labelGenerator::padToFullLabels)
                .flatMap(sampleStrategy::sample);
    }

}
