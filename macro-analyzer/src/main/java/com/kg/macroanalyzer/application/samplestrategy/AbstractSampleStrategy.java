package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractSampleStrategy extends ValidatingStrategy {

    @Override
    public Optional<AlignedBundle> sample(List<MacroSeries> macroSeriesList) {
        return Optional.ofNullable(macroSeriesList)
                .flatMap(this::validate)
                .map(this::runSamplingOnSeriesList);
    }

    private AlignedBundle runSamplingOnSeriesList(List<MacroSeries> macroSeriesList) {
        final var sampledSeries = macroSeriesList.stream()
                .map(this::runSamplingOnSeries)
                .toList();
        final var labels = sampledSeries.getFirst()
                .macroPoints().stream()
                .map(MacroPoint::date)
                .toList();

        return AlignedBundle.builder()
                .macroSeries(sampledSeries)
                .labels(labels)
                .build();
    }

    private MacroSeries runSamplingOnSeries(MacroSeries macroSeries) {
        final var sampledPoints = macroSeries.macroPoints().stream()
                .flatMap(this::matchDate)
                .collect(new UniqueMacroPointCollector());

        return macroSeries.toBuilder()
                .macroPoints(sampledPoints)
                .build();
    }

    abstract Stream<MacroPoint> matchDate(MacroPoint macroPoint);

}
