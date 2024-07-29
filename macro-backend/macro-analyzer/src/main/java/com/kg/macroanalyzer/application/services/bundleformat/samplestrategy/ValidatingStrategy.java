package com.kg.macroanalyzer.application.services.bundleformat.samplestrategy;

import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.exceptions.InvalidBundleDimensionException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class ValidatingStrategy implements SampleStrategy {

    protected Optional<List<MacroSeries>> validate(List<MacroSeries> macroSeriesList) {
        if (macroSeriesList.isEmpty()) {
            log.info("Validating bundle with empty series");
            return Optional.empty();
        }

        final var referenceSize = macroSeriesList.getFirst().macroPoints().size();
        final var withUnexpectedDimensions = macroSeriesList.stream()
                .map(MacroSeries::macroPoints)
                .filter(macroPoints -> macroPoints.size() != referenceSize)
                .count();
        final var hasDifferentDimensions = withUnexpectedDimensions > 0;
        if (hasDifferentDimensions) {
            final var msgRaw = "Expected bundle with dimension %s but found deviation";
            final var msgFormatted = msgRaw.formatted(referenceSize);
            log.error(new InvalidBundleDimensionException(msgFormatted).toString());
        }

        return hasDifferentDimensions
                ? Optional.empty()
                : Optional.of(macroSeriesList);
    }

}
