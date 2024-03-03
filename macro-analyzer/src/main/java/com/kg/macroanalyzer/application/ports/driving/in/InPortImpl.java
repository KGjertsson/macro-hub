package com.kg.macroanalyzer.application.ports.driving.in;

import com.kg.macroanalyzer.application.services.EnqueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InPortImpl implements InPort {

    private final EnqueueService enqueueService;

    @Autowired
    public InPortImpl(EnqueueService enqueueService) {
        this.enqueueService = enqueueService;
    }

    @Override
    public void enqueue(String name) {
        enqueueService.enqueue(name);
    }

    @Override
    public void enqueueAll() {
        enqueueService.enqueueAll();
    }
}
