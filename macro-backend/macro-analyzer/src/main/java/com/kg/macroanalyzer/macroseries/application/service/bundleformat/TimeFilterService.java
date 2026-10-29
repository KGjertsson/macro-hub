package com.kg.macroanalyzer.macroseries.application.service.bundleformat;

import com.kg.macroanalyzer.macroseries.application.service.bundleformat.timefilterstrategy.TimeFilterStrategyFactory;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
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
