package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;

import java.util.stream.Stream;

public abstract class AbstractScrapeEngine implements ScrapeEngine {

    private final ScrapeRepository scrapeRepository;
    private final ScrapeQueueItem scrapeQueueItem;

    public AbstractScrapeEngine(
            ScrapeRepository scrapeRepository,
            ScrapeQueueItem scrapeQueueItem
    ) {
        this.scrapeRepository = scrapeRepository;
        this.scrapeQueueItem = scrapeQueueItem;
    }

    protected void markAsDone() {
        Stream.of(scrapeQueueItem)
                .map(ScrapeQueueItem::id)
                .toList()
                .forEach(scrapeRepository::markAsDone);
    }

}
