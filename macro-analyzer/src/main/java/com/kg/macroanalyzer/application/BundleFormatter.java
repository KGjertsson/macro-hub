package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.samplestrategy.StrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public Optional<AlignedBundle> align(
            List<MacroSeries> macroSeriesList,
            StrategyFactory.Strategy strategy
    ) {
        final var sampleStrategy = strategyFactory.build(strategy);

        return Optional.ofNullable(macroSeriesList)
                .flatMap(labelGenerator::padToFullLabels)
                .flatMap(sampleStrategy::sample);
    }

}
