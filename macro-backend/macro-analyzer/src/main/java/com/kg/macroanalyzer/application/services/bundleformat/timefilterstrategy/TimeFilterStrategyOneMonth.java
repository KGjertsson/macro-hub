package com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;

import java.time.LocalDate;
import java.util.List;

public class TimeFilterStrategyOneMonth implements TimeFilterStrategy {

    LocalDate now;

    public TimeFilterStrategyOneMonth() {
        this.now = LocalDate.now();
    }

    public TimeFilterStrategyOneMonth(LocalDate now) {
        this.now = now;
    }

    @Override
    public List<MacroSeries> filter(List<MacroSeries> macroSeriesList) {
        return macroSeriesList.stream()
                .map(this::filter)
                .toList();
    }

    private MacroSeries filter(MacroSeries macroSeries) {
        final var oneMonthInThePast = now.minusMonths(1);
        final var filteredPoints = macroSeries.macroPoints().stream()
                .filter(macroPoint -> LocalDate.from(macroPoint.date()).isAfter(oneMonthInThePast))
                .toList();

        return macroSeries.toBuilder()
                .macroPoints(filteredPoints)
                .build();
    }

}
