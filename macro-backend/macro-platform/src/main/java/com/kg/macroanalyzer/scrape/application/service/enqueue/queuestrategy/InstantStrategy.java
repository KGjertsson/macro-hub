package com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy;

import java.time.Instant;

public class InstantStrategy implements QueueTimeStrategy {

    @Override
    public Instant scrapeDate(Instant now, int queuePosition) {
        return now;
    }

}
