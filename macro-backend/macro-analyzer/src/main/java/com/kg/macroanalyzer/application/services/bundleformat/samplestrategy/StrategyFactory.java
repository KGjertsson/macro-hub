package com.kg.macroanalyzer.application.services.bundleformat.samplestrategy;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StrategyFactory {

    public SampleStrategy build(Strategy strategy) {
        final var nullMsg = "Found unexpected instance of null in StrategyFactory";

        return Optional.ofNullable(strategy)
                .map(this::buildStrategy)
                .orElseThrow(() -> new IllegalArgumentException(nullMsg));
    }

    private SampleStrategy buildStrategy(@NonNull Strategy strategy) {
        return switch (strategy) {
            case YEAR -> new SampleStrategyYear();
            case MONTH -> new SampleStrategyMonth();
            case DAY -> new SampleStrategyDay();
        };
    }

    public enum Strategy {
        DAY, MONTH, YEAR
    }

}
