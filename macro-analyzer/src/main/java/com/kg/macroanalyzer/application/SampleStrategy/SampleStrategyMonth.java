package com.kg.macroanalyzer.application.SampleStrategy;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SampleStrategyMonth implements SampleStrategy {

    private final String PATTERN = "^(\\d{4}-\\d{1,2})";
    private final Pattern pattern = Pattern.compile(PATTERN);


    @Override
    public Optional<MacroBundle> sample(MacroBundle macroBundle) {
        return Optional.ofNullable(macroBundle)
                .map(MacroBundle::macroSeries)
                .map(this::sample)
                .map(this::toBundle);
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
