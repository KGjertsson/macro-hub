package com.kg.macroanalyzer.macroseries.application.service.bundleformat;

import com.kg.macroanalyzer.macroseries.application.port.in.Strategy;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy.StrategyFactory;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy.UniqueMacroPointCollector;
import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;

import java.util.List;
import java.util.Optional;

public final class Alignment {

    private Alignment() {
    }

    public static Optional<AlignedBundle> align(
            List<MacroSeries> macroSeriesList,
            Strategy strategy
    ) {
        final var sampleStrategy = StrategyFactory.build(strategy);

        return Optional.ofNullable(macroSeriesList)
                .map(Alignment::sortMacroSeries)
                .map(Alignment::removeDuplicateFromSeries)
                .flatMap(LabelPadding::padToFullLabels)
                .flatMap(sampleStrategy::sample);
    }

    private static List<MacroSeries> sortMacroSeries(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(Alignment::sortMacroPoints)
                .toList();
    }

    private static MacroSeries sortMacroPoints(MacroSeries macroSeries) {
        return macroSeries.toBuilder()
                .macroPoints(macroSeries.macroPoints().stream().sorted().toList())
                .build();
    }

    private static List<MacroSeries> removeDuplicateFromSeries(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(Alignment::removeDuplicatePoints)
                .toList();
    }

    private static MacroSeries removeDuplicatePoints(MacroSeries macroSeries) {
        return macroSeries.toBuilder()
                .macroPoints(
                        macroSeries.macroPoints()
                                .stream()
                                .collect(new UniqueMacroPointCollector())
                ).build();
    }

}
