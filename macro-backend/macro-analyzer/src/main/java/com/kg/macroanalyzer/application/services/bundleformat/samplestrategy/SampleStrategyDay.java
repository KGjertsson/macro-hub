package com.kg.macroanalyzer.application.services.bundleformat.samplestrategy;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;

import java.time.LocalDate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SampleStrategyDay extends AbstractSampleStrategy {

    private final String PATTERN = "^(\\d{4}-\\d{1,2}-\\d{1,2})";
    private final Pattern pattern = Pattern.compile(PATTERN);

    @Override
    Stream<MacroPoint> matchDate(MacroPoint macroPoint) {
        final var matcher = pattern.matcher(macroPoint.date().toString());

        if (matcher.find()) {
            final var matchedGroup = matcher.group(0);
            final var localDate = LocalDate.parse(matchedGroup);
            final var sampledPoint = macroPoint.toBuilder()
                    .date(localDate)
                    .build();
            return Stream.of(sampledPoint);
        } else {
            return Stream.empty();
        }
    }

}
