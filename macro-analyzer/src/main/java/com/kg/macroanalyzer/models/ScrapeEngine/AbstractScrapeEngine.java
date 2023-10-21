package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class AbstractScrapeEngine<T> implements ScrapeEngine {

    private final ScrapeRepository scrapeRepository;
    private final ScrapeQueueItem scrapeQueueItem;

    public AbstractScrapeEngine(
            ScrapeRepository scrapeRepository,
            ScrapeQueueItem scrapeQueueItem
    ) {
        this.scrapeRepository = scrapeRepository;
        this.scrapeQueueItem = scrapeQueueItem;
    }

    @Override
    public Integer scrape() {
        try {
            final var scraped = scrapeItems();
            final var inserted = insertScrapedItems(scraped);
            this.markAsDone();

            return inserted;
        } catch (IOException | RuntimeException e) {
            log.error("Exception while attempting to scrape data: %s".formatted(e.getMessage()));

            return 0;
        }
    }

    protected abstract List<T> scrapeItems() throws IOException;

    protected abstract Integer insertScrapedItems(List<T> scraped);

    protected void markAsDone() {
        scrapeRepository.markAsDone(scrapeQueueItem.id());
    }

}
