package com.kg.macroanalyzer.application.ports.driving.in;

public interface InPort {

    void enqueue(String name);

    void enqueueAll();

}
