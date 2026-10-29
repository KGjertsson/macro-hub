package com.kg.macroanalyzer.scrape.application.port.in;

import com.kg.macroanalyzer.scrape.application.port.in.EnqueueException;
import com.kg.macroanalyzer.scrape.application.service.ScrapeRun;

import java.time.LocalDateTime;

public interface InPort {

    void enqueue(String name) throws EnqueueException;

    void enqueueAll();

    ScrapeRun scrapeFromQueue(LocalDateTime timeStamp);

}
