package com.kg.macroanalyzer.scrape.application.port.in;

import com.kg.macroanalyzer.scrape.domain.ScrapeResult;

import java.util.List;

public sealed interface ScrapeRun {

    record Completed(List<ScrapeResult> results) implements ScrapeRun {
    }

    // Another scrape holds the lock; nothing was scraped
    record AlreadyRunning() implements ScrapeRun {
    }

}
