package com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;

import java.time.LocalDate;
import java.util.List;

public class TimeFilterStrategyYear implements TimeFilterStrategy {

    private final LocalDate now;
    private final Integer years;

    public TimeFilterStrategyYear(Integer years) {
        this.now = LocalDate.now();
        this.years = years;
    }

    public TimeFilterStrategyYear(LocalDate now, Integer years) {
        this.now = now;
        this.years = years;
    }

    @Override
    public List<MacroSeries> filter(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(this::filter)
                .toList();
    }

    private MacroSeries filter(MacroSeries macroSeries) {
        final var oneYearInThePast = now.minusYears(this.years);
        final var filteredPoints = macroSeries.macroPoints().stream()
                .filter(macroPoint -> LocalDate.from(macroPoint.date()).isAfter(oneYearInThePast))
                .toList();

        return macroSeries.toBuilder()
                .macroPoints(filteredPoints)
                .build();
    }

}
