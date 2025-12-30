package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;

public interface Persister<Data> {
    ScrapeResult persist(Data scraped);
}
