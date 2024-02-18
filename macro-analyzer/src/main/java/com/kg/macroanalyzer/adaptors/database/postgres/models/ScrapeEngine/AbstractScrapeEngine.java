package com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
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
            log.error("Exception while attempting to scrape values: %s".formatted(e.getMessage()));

            return 0;
        }
    }

    protected abstract List<T> scrapeItems() throws IOException;

    protected abstract Integer insertScrapedItems(List<T> scraped);

    protected void markAsDone() {
        log.info("Marking scrape queue item %s as done".formatted(scrapeQueueItem.id()));
        scrapeRepository.markAsDone(scrapeQueueItem.id());
    }

}
