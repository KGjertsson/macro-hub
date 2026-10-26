package com.kg.macroanalyzer.application.ports.driving.in;

import com.kg.macroanalyzer.application.exceptions.EnqueueException;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InPort {

    void enqueue(String name) throws EnqueueException;

    void enqueueAll();

    Optional<List<ScrapeResult>> scrapeFromQueue(LocalDateTime timeStamp);

}
