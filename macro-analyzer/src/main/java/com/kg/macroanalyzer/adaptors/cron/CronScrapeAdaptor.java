package com.kg.macroanalyzer.adaptors.cron;

import com.kg.macroanalyzer.application.ports.driving.in.InPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class CronScrapeAdaptor {

    private final InPort inPort;

    @Autowired
    public CronScrapeAdaptor(InPort inPort) {
        this.inPort = inPort;
    }

    @Scheduled(fixedDelay = 300000)
    public void scrape() {
        log.info("Cron: scraping all applicable items from the queue");

        try {
            final var timeStamp = LocalDateTime.now(ZoneOffset.UTC);

            inPort.scrapeFromQueue(timeStamp);
        } catch (Throwable t) {
            log.error("Received unexpected error when attempting to scrape items from queue", t);
        }
    }

}
