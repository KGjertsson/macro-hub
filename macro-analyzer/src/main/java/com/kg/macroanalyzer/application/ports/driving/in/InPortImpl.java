package com.kg.macroanalyzer.application.ports.driving.in;

import com.kg.macroanalyzer.adaptors.http.services.ScrapeService;
import com.kg.macroanalyzer.application.services.enqueue.EnqueueService;
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
    public void enqueue(String name) {
        enqueueService.enqueue(name);
    }

    @Override
    public void enqueueAll() {
        enqueueService.enqueueAll();
    }

    @Override
    public void scrapeFromQueue(LocalDateTime timeStamp) {
        scrapeService.scrapeFromQueue(timeStamp);
    }
}
