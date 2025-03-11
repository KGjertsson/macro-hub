package com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TimeFilterStrategyFactory {

    public TimeFilterStrategy build(TimeFrame timeFrame) {
        final var nullMsg = "Found unexpected instance of null in TimeFilterStrategy";

        return Optional.ofNullable(timeFrame)
                .map(this::buildStrategy)
                .orElseThrow(() -> new IllegalArgumentException(nullMsg));
    }

    public enum TimeFrame {
        ALL, ONEYEAR, FIVEYEAR, TENYEAR, ONEMONTH
    }

    private TimeFilterStrategy buildStrategy(TimeFrame timeFrame) {
        return switch (timeFrame) {
            case ALL -> new TimeFilterStrategyAll();
            case ONEYEAR -> new TimeFilterStrategyYear(1);
            case FIVEYEAR -> new TimeFilterStrategyYear(5);
            case TENYEAR -> new TimeFilterStrategyYear(10);
            case ONEMONTH -> new TimeFilterStrategyOneMonth();
        };
    }

}
