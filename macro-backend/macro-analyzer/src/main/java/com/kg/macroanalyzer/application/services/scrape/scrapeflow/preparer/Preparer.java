package com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer;

import java.util.Optional;

public interface Preparer<Input, Config> {
    Optional<Config> prepare(Input input);
}
