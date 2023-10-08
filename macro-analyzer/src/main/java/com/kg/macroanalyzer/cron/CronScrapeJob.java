package com.kg.macroanalyzer.cron;

import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEngine;
import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEngineFactory;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CronScrapeJob {

    private final Integer SCRAPE_DELAY = 300000;
    private final ScrapeRepository scrapeRepository;
    private final ScrapeEngineFactory scrapeEngineFactory;

    @Autowired
    public CronScrapeJob(
            ScrapeRepository scrapeRepository,
            ScrapeEngineFactory scrapeEngineFactory) {
        this.scrapeRepository = scrapeRepository;
        this.scrapeEngineFactory = scrapeEngineFactory;
    }

    @Scheduled(fixedDelay = SCRAPE_DELAY)
    public void scrape() {
        final var now = LocalDateTime.now();

        scrapeRepository.getItemsToScrape(now)
                .stream()
                .map(scrapeEngineFactory::createScrapeEngine)
                .filter(Objects::nonNull)
                .forEach(ScrapeEngine::scrape);
    }

}
