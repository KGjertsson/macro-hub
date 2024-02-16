package com.kg.macroanalyzer.application.samplestrategy;

import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {

    public SampleStrategy build(Strategy strategy) {
        return switch (strategy) {
            case YEAR -> new SampleStrategyYear();
            case MONTH -> new SampleStrategyMonth();
            default -> throw new IllegalArgumentException("prutt");
        };
    }

    public enum Strategy {
        YEAR, MONTH, DAY
    }

}
