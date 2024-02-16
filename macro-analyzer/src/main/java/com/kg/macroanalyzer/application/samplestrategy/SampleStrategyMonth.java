package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class SampleStrategyMonth extends AbstractSampleStrategy {

    private final String PATTERN = "^(\\d{4}-\\d{1,2})";
    private final Pattern pattern = Pattern.compile(PATTERN);

    @Override
    protected Stream<MacroPoint> matchDate(MacroPoint macroPoint) {
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
