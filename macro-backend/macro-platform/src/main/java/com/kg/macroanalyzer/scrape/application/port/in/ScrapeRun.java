package com.kg.macroanalyzer.scrape.application.port.in;

import com.kg.macroanalyzer.scrape.domain.ScrapeResult;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;

public sealed interface ScrapeRun {

    record Completed(List<ScrapeResult> results) implements ScrapeRun {

        public Completed {
            results = ImmutableCopies.list(results);
        }

    }

    record AlreadyRunning() implements ScrapeRun {
    }

}
