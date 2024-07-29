package com.kg.macroanalyzer.application.ports.driving.in;

import com.kg.macroanalyzer.application.exceptions.EnqueueException;
import com.kg.macroanalyzer.application.services.enqueue.EnqueueService;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import com.kg.macroanalyzer.application.services.scrape.ScrapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<ScrapeResult> scrapeFromQueue(LocalDateTime timeStamp) {
        return scrapeService.scrapeFromQueue(timeStamp);
    }
}
