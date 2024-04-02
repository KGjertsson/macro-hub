package com.kg.macroanalyzer.application.services.bundleformat.samplestrategy;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StrategyFactory {

    public SampleStrategy build(Strategy strategy) {
        final var nullMsg = "Found unexpected instance of null in StrategyFactory";

        return Optional.ofNullable(strategy)
                .map(this::createClass)
                .orElseThrow(() -> new IllegalArgumentException(nullMsg));
    }

    private SampleStrategy createClass(@NonNull Strategy strategy) {
        final var errorMsg = "Found invalid strategy %s".formatted(strategy);

        return switch (strategy) {
            case YEAR -> new SampleStrategyYear();
            case MONTH -> new SampleStrategyMonth();
            default -> throw new IllegalArgumentException(errorMsg);
        };
    }

    public enum Strategy {
        DAY, MONTH, YEAR
    }

}
