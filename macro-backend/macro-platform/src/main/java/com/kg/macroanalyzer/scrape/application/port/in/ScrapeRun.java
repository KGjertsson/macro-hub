package com.kg.macroanalyzer.scrape.application.port.in;

import com.kg.macroanalyzer.scrape.domain.ScrapeResult;

import java.util.List;

public sealed interface ScrapeRun {

    record Completed(List<ScrapeResult> results) implements ScrapeRun {
    }

    record AlreadyRunning() implements ScrapeRun {
    }

}
