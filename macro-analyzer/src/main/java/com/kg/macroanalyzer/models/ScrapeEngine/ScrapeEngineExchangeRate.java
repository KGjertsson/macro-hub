package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.ExchangeRateUsdSek;
import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ExchangeRateRepository;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ScrapeEngineExchangeRate extends AbstractScrapeEngine {

    private final ExchangeRateRepository exchangeRateRepository;

    public ScrapeEngineExchangeRate(
            ScrapeQueueItem scrapeQueueItem,
            ScrapeRepository scrapeRepository,
            ExchangeRateRepository exchangeRateRepository

    ) {
        super(scrapeRepository, scrapeQueueItem);
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Override
    public Integer scrape() {
        try {
            final var novelScrapedItems = scrapeItems();
            insertScrapedItems(novelScrapedItems);
            this.markAsDone();

            return novelScrapedItems.size();
        } catch (IOException ioException) {
            final var msgRaw = "Received IOException while scraping usd-sek exchange rate: %s";
            final var msgFormatted = msgRaw.formatted(ioException.getMessage());
            log.error(msgFormatted);

            return 0;
        }
    }

    private List<ExchangeRateUsdSek> scrapeItems() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04";
        final var items = exchangeRateRepository.getExchangeRateUsdSek();

        return ScrapeUtils.scrapeNovelItems(
                url,
                items,
                ExchangeRateUsdSek.class
        );
    }

    private void insertScrapedItems(List<ExchangeRateUsdSek> novelScrapedItems) {
        exchangeRateRepository.insertExchangeRateUsdSek(novelScrapedItems);
        final var msgRaw = "Found %s new items from scraping, persisting do db...";
        final var msgFormatted = msgRaw.formatted(novelScrapedItems.size());
        log.info(msgFormatted);
    }

}
