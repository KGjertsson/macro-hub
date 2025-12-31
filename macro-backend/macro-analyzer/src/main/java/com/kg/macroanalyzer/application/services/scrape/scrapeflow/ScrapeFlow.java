package com.kg.macroanalyzer.application.services.scrape.scrapeflow;

import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder.ExistingFinder;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister.Persister;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer.Preparer;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper.Scraper;

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
