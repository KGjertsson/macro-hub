package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import com.kg.macroanalyzer.scrape.domain.ScrapeResult;

public interface Persister<Data> {

    ScrapeResult persist(Data scraped);

}
