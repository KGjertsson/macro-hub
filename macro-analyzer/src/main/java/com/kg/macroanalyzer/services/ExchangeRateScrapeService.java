package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.ExchangeRateUsdSek;
import com.kg.macroanalyzer.repositories.ExchangeRateRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ExchangeRateScrapeService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ScrapeUtils scrapeUtils;

    @Autowired
    public ExchangeRateScrapeService(
            ExchangeRateRepository exchangeRateRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.scrapeUtils = scrapeUtils;
    }

    public Integer scrapeExchangeRateUsdSek() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04";
        final var persisted = exchangeRateRepository.getExchangeRateUsdSek();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, ExchangeRateUsdSek.class);
        exchangeRateRepository.insertExchangeRateUsdSek(scraped);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));

        return scraped.size();
    }

}
