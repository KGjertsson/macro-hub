package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import java.util.Optional;

public interface Scraper<Data> {
    Optional<Data> scrape(Data existing);
}
