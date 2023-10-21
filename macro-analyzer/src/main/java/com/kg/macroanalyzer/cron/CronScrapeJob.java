package com.kg.macroanalyzer.cron;

import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEngine;
import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEngineFactory;
import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class CronScrapeJob {

    private final ScrapeRepository scrapeRepository;
    private final ScrapeEngineFactory scrapeEngineFactory;

    @Autowired
    public CronScrapeJob(
            ScrapeRepository scrapeRepository,
            ScrapeEngineFactory scrapeEngineFactory) {
        this.scrapeRepository = scrapeRepository;
        this.scrapeEngineFactory = scrapeEngineFactory;
    }

    @Scheduled(fixedDelay = 300000)
    public void scrape() {
        fetchItemsToScrape()
                .map(scrapeEngineFactory::createScrapeEngine)
                .filter(Objects::nonNull)
                .forEach(ScrapeEngine::scrape);
    }

    private Stream<ScrapeQueueItem> fetchItemsToScrape() {
        final var now = LocalDateTime.now(ZoneOffset.UTC);
        final var items = scrapeRepository.getItemsToScrape(now);
        log.info("Running scrape cron job with %s items".formatted(items));

        return items.stream();
    }

}
