package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.ports.driving.in.InPort;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class InsertionAdaptor {

    private final InPort inPort;

    @Autowired
    public InsertionAdaptor(InPort inPort) {
        this.inPort = inPort;
    }

    @PostMapping("/enqueue")
    public ResponseEntity<Void> enqueue(@RequestParam("name") String name) {
        log.info("Received enqueue request with name=%s".formatted(name));
        try {
            inPort.enqueue(name);

            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Throwable t) {
            log.error("Received unexpected error when processing enqueue request", t);

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/enqueue-all")
    public ResponseEntity<Void> enqueueAll() {
        log.info("Received enqueue all request");
        try {
            inPort.enqueueAll();

            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Throwable t) {
            log.error("Received unexpected error when processing enqueue all request", t);

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/scrape-from-queue")
    public ResponseEntity<Void> scrapeFromQueue() {
        log.info("Received request to scrape from queue");

        try {
            final var timeStamp = LocalDateTime.now(ZoneOffset.UTC);
            final var scrapeResult = inPort.scrapeFromQueue(timeStamp);
            logScrapeResult(scrapeResult);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Throwable t) {
            log.error("Received unexpected error when attempting to scrape from queue", t);

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void logScrapeResult(List<ScrapeResult> scrapeResultList) {
        final var success = scrapeResultList.stream()
                .filter(r -> r.equals(ScrapeResult.SUCCESS))
                .count();
        final var empty = scrapeResultList.stream()
                .filter(r -> r.equals(ScrapeResult.EMPTY))
                .count();
        final var failed = scrapeResultList.stream()
                .filter(r -> r.equals(ScrapeResult.FAILED))
                .count();
        final var msgRaw = "Scrape result: SUCCESS: %s, EMPTY: %S, FAILED: %s";
        final var msg = msgRaw.formatted(success, empty, failed);
        log.info(msg);
    }

}
