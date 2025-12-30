package com.kg.macroanalyzer.application.services.bundleformat;

import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy.TimeFilterStrategyFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TimeFilterService {

    private final TimeFilterStrategyFactory timeFilterStrategyFactory;

    public TimeFilterService(TimeFilterStrategyFactory timeFilterStrategyFactory) {
        this.timeFilterStrategyFactory = timeFilterStrategyFactory;
    }

    public Optional<List<MacroSeries>> shrinkToTimeFrame(
            List<MacroSeries> macroSeriesList,
            TimeFilterStrategyFactory.TimeFrame timeFrame
    ) {
        final var timeFilterStrategy = timeFilterStrategyFactory.build(timeFrame);

        return Optional.ofNullable(macroSeriesList)
                .map(timeFilterStrategy::filter);
    }

}
