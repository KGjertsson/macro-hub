package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import java.util.Optional;

public interface Scraper<Data> {
    Optional<Data> scrape(Data existing);
}
