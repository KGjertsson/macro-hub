package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.ports.driving.in.InPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
