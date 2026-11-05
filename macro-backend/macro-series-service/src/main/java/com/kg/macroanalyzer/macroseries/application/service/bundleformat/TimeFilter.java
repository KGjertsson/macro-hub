package com.kg.macroanalyzer.macroseries.application.service.bundleformat;

import com.kg.macroanalyzer.macroseries.application.port.in.TimeFrame;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.timefilterstrategy.TimeFilterStrategyFactory;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;

import java.util.List;
import java.util.Optional;

public final class TimeFilter {

    private TimeFilter() {
    }

    public static Optional<List<MacroSeries>> shrinkToTimeFrame(
            List<MacroSeries> macroSeriesList,
            TimeFrame timeFrame
    ) {
        final var timeFilterStrategy = TimeFilterStrategyFactory.build(timeFrame);

        return Optional.ofNullable(macroSeriesList)
                .map(timeFilterStrategy::filter);
    }

}
