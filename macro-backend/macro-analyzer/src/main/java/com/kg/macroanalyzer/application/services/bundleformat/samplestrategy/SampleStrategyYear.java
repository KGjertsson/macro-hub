package com.kg.macroanalyzer.application.services.bundleformat.samplestrategy;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;

import java.time.Year;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SampleStrategyYear extends AbstractSampleStrategy {

    private final String PATTERN = "^(\\d{4})";
    private final Pattern pattern = Pattern.compile(PATTERN);

    @Override
    protected Stream<MacroPoint> matchDate(MacroPoint macroPoint) {
        final var matcher = pattern.matcher(macroPoint.date().toString());

        if (matcher.find()) {
            final var matchedGroup = matcher.group(0);
            final var year = Year.parse(matchedGroup);
            final var sampledPoint = macroPoint.toBuilder()
                    .date(year)
                    .build();
            return Stream.of(sampledPoint);
        } else {
            return Stream.empty();
        }
    }

}
