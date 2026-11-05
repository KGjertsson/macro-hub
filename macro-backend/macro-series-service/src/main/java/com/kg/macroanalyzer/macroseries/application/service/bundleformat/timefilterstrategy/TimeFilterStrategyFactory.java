package com.kg.macroanalyzer.macroseries.application.service.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.macroseries.application.port.in.TimeFrame;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class TimeFilterStrategyFactory {

    public static TimeFilterStrategy build(TimeFrame timeFrame) {
        final var nullMsg = "Found unexpected instance of null in TimeFilterStrategy";

        return Optional.ofNullable(timeFrame)
                .map(TimeFilterStrategyFactory::buildStrategy)
                .orElseThrow(() -> new IllegalArgumentException(nullMsg));
    }

    private static TimeFilterStrategy buildStrategy(TimeFrame timeFrame) {
        return switch (timeFrame) {
            case ALL -> new TimeFilterStrategyAll();
            case ONEYEAR -> new TimeFilterStrategyYear(1);
            case FIVEYEAR -> new TimeFilterStrategyYear(5);
            case TENYEAR -> new TimeFilterStrategyYear(10);
            case ONEMONTH -> new TimeFilterStrategyOneMonth();
        };
    }

}
