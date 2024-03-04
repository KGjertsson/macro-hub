package com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ExchangeRateRepository;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ScrapeEngineExchangeRate extends AbstractScrapeEngine<MacroPoint> {

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
    protected List<MacroPoint> scrapeItems() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04";
        final var items = exchangeRateRepository.getExchangeRateUsdSek().get();

        return ScrapeUtils.scrapeNovelItems(
                url,
                items,
                MacroPoint.class
        );
    }

    @Override
    protected Integer insertScrapedItems(List<MacroPoint> novelScrapedItems) {
        final var msgRaw = "Found %s new items from scraping, persisting do db...";
        final var msgFormatted = msgRaw.formatted(novelScrapedItems.size());
        log.info(msgFormatted);

        return exchangeRateRepository.insertExchangeRateUsdSek().apply(novelScrapedItems);
    }

}
