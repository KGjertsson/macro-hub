package com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.application.domain.MacroSeries;

import java.time.LocalDate;
import java.util.List;

public class TimeFilterStrategyOneYear implements TimeFilterStrategy {

    private LocalDate now;

    public TimeFilterStrategyOneYear() {
        this.now = LocalDate.now();
    }

    public TimeFilterStrategyOneYear(LocalDate now) {
        this.now = now;
    }

    @Override
    public List<MacroSeries> filter(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(this::filter)
                .toList();
    }

    private MacroSeries filter(MacroSeries macroSeries) {
        final var oneYearInThePast = now.minusYears(1);
        final var filteredPoints = macroSeries.macroPoints().stream()
                .filter(macroPoint -> LocalDate.from(macroPoint.date()).isAfter(oneYearInThePast))
                .toList();

        return macroSeries.toBuilder()
                .macroPoints(filteredPoints)
                .build();
    }

}
