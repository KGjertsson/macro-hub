package com.kg.macroanalyzer.application.services.bundleformat;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.services.LabelGenerationService;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.StrategyFactory;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.UniqueMacroPointCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BundleFormatService {

    private final LabelGenerationService labelGenerationService;
    private final StrategyFactory strategyFactory;

    @Autowired
    public BundleFormatService(
            LabelGenerationService labelGenerationService,
            StrategyFactory strategyFactory
    ) {
        this.labelGenerationService = labelGenerationService;
        this.strategyFactory = strategyFactory;
    }

    public Optional<AlignedBundle> align(
            List<MacroSeries> macroSeriesList,
            StrategyFactory.Strategy strategy
    ) {
        final var sampleStrategy = strategyFactory.build(strategy);

        return Optional.ofNullable(macroSeriesList)
                .map(this::sortMacroSeries)
                .map(this::removeDuplicateFromSeries)
                .flatMap(labelGenerationService::padToFullLabels)
                .flatMap(sampleStrategy::sample);
    }

    private List<MacroSeries> sortMacroSeries(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(this::sortMacroPoints)
                .toList();
    }

    private MacroSeries sortMacroPoints(MacroSeries macroSeries) {
        return macroSeries.toBuilder()
                .macroPoints(macroSeries.macroPoints().stream().sorted().toList())
                .build();
    }

    private List<MacroSeries> removeDuplicateFromSeries(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(this::removeDuplicatePoints)
                .toList();
    }

    private MacroSeries removeDuplicatePoints(MacroSeries macroSeries) {
        return macroSeries.toBuilder()
                .macroPoints(
                        macroSeries.macroPoints()
                                .stream()
                                .collect(new UniqueMacroPointCollector())
                ).build();
    }

}
