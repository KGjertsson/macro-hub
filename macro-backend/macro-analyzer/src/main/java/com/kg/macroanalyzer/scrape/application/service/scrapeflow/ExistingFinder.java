package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import java.util.Optional;

public interface ExistingFinder<Config, Data> {
    Optional<Data> findExisting(Config prepared);
}
