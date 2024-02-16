package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.exceptions.InvalidBundleDimensionException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class SampleStrategyMonth implements SampleStrategy {

    private final String PATTERN = "^(\\d{4}-\\d{1,2})";
    private final Pattern pattern = Pattern.compile(PATTERN);


    @Override
    public Optional<MacroBundle> sample(
            MacroBundle macroBundle
    ) {
        return Optional.ofNullable(macroBundle)
                .flatMap(this::validateBundle)
                .map(MacroBundle::macroSeries)
                .map(this::sample)
                .map(this::toBundle);
    }

    private Optional<MacroBundle> validateBundle(
            MacroBundle macroBundle
    ) {
        if (macroBundle.macroSeries().isEmpty()) {
            log.info("Validating bundle with empty series");
            return Optional.empty();
        }

        final var referenceSize = macroBundle.macroSeries().getFirst().macroPoints().size();
        final var withUnexpectedDimensions = macroBundle.macroSeries().stream()
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
                : Optional.of(macroBundle);
    }

    private List<MacroSeries> sample(List<MacroSeries> macroSeries) {
        return macroSeries.stream()
                .map(this::sample)
                .toList();
    }

    private MacroSeries sample(MacroSeries macroSeries) {
        final var sampledPoints = macroSeries.macroPoints().stream()
                .filter(this::sampleFilter)
                .collect(new UniqueMacroPointCollector());

        return macroSeries.toBuilder()
                .macroPoints(sampledPoints)
                .build();
    }

    private boolean sampleFilter(MacroPoint macroPoint) {
        // TODO
        final var matcher = pattern.matcher(macroPoint.date().toString());

        if (matcher.find()) {
            String matchedString = matcher.group(1);
            System.out.println("Matched string: " + matchedString);
        } else {
            System.out.println("No match found.");
        }

        return false;
    }

    private MacroBundle toBundle(List<MacroSeries> macroSeries) {
        return MacroBundle.builder()
                .macroSeries(macroSeries)
                .build();
    }

}
