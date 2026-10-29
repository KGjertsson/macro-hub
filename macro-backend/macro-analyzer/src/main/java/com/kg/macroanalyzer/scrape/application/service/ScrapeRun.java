package com.kg.macroanalyzer.scrape.application.service;

import java.util.List;

public sealed interface ScrapeRun {

    record Completed(List<ScrapeResult> results) implements ScrapeRun {
    }

    // Another scrape holds the lock; nothing was scraped
    record AlreadyRunning() implements ScrapeRun {
    }

}
