package com.kg.macroanalyzer.application.services.bundleformatservice.samplestrategy;

import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {

    public SampleStrategy build(Strategy strategy) {
        final var errorMsg = "Found invalid strategy %s".formatted(strategy);

        return switch (strategy) {
            case YEAR -> new SampleStrategyYear();
            case MONTH -> new SampleStrategyMonth();
            default -> throw new IllegalArgumentException(errorMsg);
        };
    }

    public enum Strategy {
        YEAR, MONTH, DAY
    }

}
