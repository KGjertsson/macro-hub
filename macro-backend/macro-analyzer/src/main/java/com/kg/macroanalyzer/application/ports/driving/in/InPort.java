package com.kg.macroanalyzer.application.ports.driving.in;

import com.kg.macroanalyzer.application.exceptions.EnqueueException;
import com.kg.macroanalyzer.application.services.scrape.ScrapeRun;

import java.time.LocalDateTime;

public interface InPort {

    void enqueue(String name) throws EnqueueException;

    void enqueueAll();

    ScrapeRun scrapeFromQueue(LocalDateTime timeStamp);

}
