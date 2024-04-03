package com.kg.macroanalyzer.application.services.enqueue.queuestrategy;

public class QueueStrategyFactory {

    public static QueueTimeStrategy buildQueueStrategy(Strategy strategy) {
        return switch (strategy) {
            case INSTANT -> new InstantStrategy();
            case BACKOFF -> new ConstantBackoffStrategy();
        };
    }

    public enum Strategy {
        INSTANT,
        BACKOFF
    }

}
