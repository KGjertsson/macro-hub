package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import java.util.Optional;

public interface ExistingFinder<Config, Data> {
    Optional<Data> find(Config prepared);
}
