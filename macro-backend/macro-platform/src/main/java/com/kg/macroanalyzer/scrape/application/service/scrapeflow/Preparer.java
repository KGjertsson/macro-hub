package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import java.util.Optional;

public interface Preparer<Input, Config> {

    Optional<Config> prepare(Input input);

}
