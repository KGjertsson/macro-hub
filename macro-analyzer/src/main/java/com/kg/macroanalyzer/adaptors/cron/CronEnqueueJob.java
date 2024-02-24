package com.kg.macroanalyzer.adaptors.cron;

import com.kg.macroanalyzer.adaptors.http.services.ScrapeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class CronEnqueueJob {

    final ScrapeService scrapeService;

    public CronEnqueueJob(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    //    @Scheduled(cron = "0 22 * * * ?")
    public void enqueue() {
        scrapeService.scheduleAll(ChronoUnit.MINUTES, 5);
    }

}
