package com.kg.macroanalyzer.application.ports.driving.in;

import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;

import java.time.LocalDateTime;
import java.util.List;

public interface InPort {

    void enqueue(String name);

    void enqueueAll();

    List<ScrapeResult> scrapeFromQueue(LocalDateTime timeStamp);

}
