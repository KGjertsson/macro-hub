package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import com.kg.macroanalyzer.scrape.domain.ScrapeResult;

public record ScrapeFlow<Input, Config, Data>(
        Preparer<Input, Config> preparer,
        ExistingFinder<Config, Data> finder,
        Scraper<Data> scraper,
        Persister<Data> persister
) {

    public ScrapeResult run(Input input) {
        return preparer.prepare(input)
                .flatMap(finder::findExisting)
                .flatMap(scraper::scrape)
                .map(persister::persist)
                .orElse(ScrapeResult.FAILED);
    }

}
