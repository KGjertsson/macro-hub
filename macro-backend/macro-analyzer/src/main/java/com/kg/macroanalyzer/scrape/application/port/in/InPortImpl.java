package com.kg.macroanalyzer.scrape.application.port.in;

import com.kg.macroanalyzer.scrape.application.service.ScrapeRun;
import com.kg.macroanalyzer.scrape.application.service.ScrapeService;
import com.kg.macroanalyzer.scrape.application.service.enqueue.EnqueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InPortImpl implements InPort {

    private final EnqueueService enqueueService;
    private final ScrapeService scrapeService;

    @Autowired
    public InPortImpl(EnqueueService enqueueService, ScrapeService scrapeService) {
        this.enqueueService = enqueueService;
        this.scrapeService = scrapeService;
    }

    @Override
    public void enqueue(String name) throws EnqueueException {
        enqueueService.enqueue(name);
    }

    @Override
    public void enqueueAll() {
        enqueueService.enqueueAll();
    }

    @Override
    public ScrapeRun scrapeFromQueue(LocalDateTime timeStamp) {
        return scrapeService.scrapeFromQueue(timeStamp);
    }

}
