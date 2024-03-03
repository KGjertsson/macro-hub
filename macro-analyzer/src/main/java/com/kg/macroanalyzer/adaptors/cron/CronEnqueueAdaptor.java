package com.kg.macroanalyzer.adaptors.cron;

import com.kg.macroanalyzer.application.ports.driving.in.InPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CronEnqueueAdaptor {

    final InPort inPort;

    public CronEnqueueAdaptor(InPort inPort) {
        this.inPort = inPort;
    }

    @Scheduled(cron = "0 * 22 * * ?")
    public void enqueueAll() {
        log.info("Cron: pushing all available series to scrape queue");

        try {
            inPort.enqueueAll();
        } catch (Throwable t) {
            log.error("Received unexpected error when enqueueing all available series", t);
        }
    }

}
