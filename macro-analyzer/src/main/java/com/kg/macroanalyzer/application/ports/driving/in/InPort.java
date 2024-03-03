package com.kg.macroanalyzer.application.ports.driving.in;

import java.time.LocalDateTime;

public interface InPort {

    void enqueue(String name);

    void enqueueAll();

    void scrapeFromQueue(LocalDateTime timeStamp);

}
