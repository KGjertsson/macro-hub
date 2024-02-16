package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.exceptions.InvalidBundleDimensionException;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
                .map(this::runSampling);
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

    private MacroBundle runSampling(MacroBundle macroBundle) {
        final var sampledSeries = macroBundle.macroSeries().stream()
                .map(this::runSampling)
                .toList();

        return MacroBundle.builder()
                .macroSeries(sampledSeries)
                .build();
    }

    private MacroSeries runSampling(MacroSeries macroSeries) {
        final var sampledPoints = macroSeries.macroPoints().stream()
                .flatMap(this::matchDate)
                .collect(new UniqueMacroPointCollector());

        return macroSeries.toBuilder()
                .macroPoints(sampledPoints)
                .build();
    }

    private Stream<MacroPoint> matchDate(MacroPoint macroPoint) {
        final var matcher = pattern.matcher(macroPoint.date().toString());

        if (matcher.find()) {
            final var matchedGroup = matcher.group(0);
            final var yearMonth = YearMonth.parse(matchedGroup);
            final var sampledPoint = macroPoint.toBuilder()
                    .date(yearMonth)
                    .build();
            return Stream.of(sampledPoint);
        } else {
            return Stream.empty();
        }
    }

}
