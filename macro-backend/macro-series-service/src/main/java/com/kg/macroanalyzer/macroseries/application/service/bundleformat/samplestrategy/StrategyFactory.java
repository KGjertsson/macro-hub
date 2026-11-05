package com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy;

import com.kg.macroanalyzer.macroseries.application.port.in.Strategy;
import lombok.NonNull;

import java.util.Optional;

public final class StrategyFactory {

    private StrategyFactory() {
    }

    public static SampleStrategy build(Strategy strategy) {
        final var nullMsg = "Found unexpected instance of null in StrategyFactory";

        return Optional.ofNullable(strategy)
                .map(StrategyFactory::buildStrategy)
                .orElseThrow(() -> new IllegalArgumentException(nullMsg));
    }

    private static SampleStrategy buildStrategy(@NonNull Strategy strategy) {
        return switch (strategy) {
            case YEAR -> new SampleStrategyYear();
            case MONTH -> new SampleStrategyMonth();
            case DAY -> new SampleStrategyDay();
        };
    }

}
