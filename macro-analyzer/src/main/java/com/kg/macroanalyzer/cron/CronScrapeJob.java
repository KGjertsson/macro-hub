package com.kg.macroanalyzer.cron;

import com.kg.macroanalyzer.repositories.ScrapeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CronScrapeJob {

    private final ScrapeRepository scrapeRepository;

    @Autowired
    public CronScrapeJob(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    @Scheduled(fixedDelay = 300000)
    public void scrape() {
        final var now = LocalDateTime.now();
        final var datasetsToScrape = scrapeRepository.getItemsToScrape(now);

        // TODO: do something fancy with the datasets, maybe template method?
    }

}
