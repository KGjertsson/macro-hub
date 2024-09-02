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
        ALL, ONEYEAR, ONEMONTH
    }

    private TimeFilterStrategy buildStrategy(TimeFrame timeFrame) {
        return switch (timeFrame) {
            case ALL -> new TimeFilterStrategyAll();
            case ONEYEAR -> new TimeFilterStrategyOneYear();
            case ONEMONTH -> new TimeFilterStrategyOneMonth();
        };
    }

}
