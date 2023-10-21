package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.EuroMarketRateItem;
import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ScrapeEngineEuroMarketRate extends AbstractScrapeEngine {

    private final String url;
    private final Supplier<List<EuroMarketRateItem>> euroMarketRateReadSupplier;
    private final Function<List<EuroMarketRateItem>, Integer> euroMarketRateWriteSupplier;

    public ScrapeEngineEuroMarketRate(
            ScrapeRepository scrapeRepository,
            ScrapeQueueItem scrapeQueueItem,
            String url,
            Supplier<List<EuroMarketRateItem>> euroMarketRateReadSupplier,
            Function<List<EuroMarketRateItem>, Integer> euroMarketRateWriteSupplier
    ) {
        super(scrapeRepository, scrapeQueueItem);
        final var baseUrl = "https://api-test.riksbank.se/swea/v1/Observations";

        this.url = baseUrl + url;
        this.euroMarketRateReadSupplier = euroMarketRateReadSupplier;
        this.euroMarketRateWriteSupplier = euroMarketRateWriteSupplier;
    }

    @Override
    public Integer scrape() {
        try {
            final var scraped = scrapeItems();
            final var inserted = insertScrapedItems(scraped);
            this.markAsDone();

            return inserted;
        } catch (IOException ioException) {

            return 0;
        }
    }

    private List<EuroMarketRateItem> scrapeItems() throws IOException {
        final var existingEuroMarketRateItems = euroMarketRateReadSupplier.get();
        return ScrapeUtils.scrapeNovelItems(
                url,
                existingEuroMarketRateItems,
                EuroMarketRateItem.class
        );
    }

    private Integer insertScrapedItems(List<EuroMarketRateItem> scraped) {
        final var msgRaw = "Found %s new items from scraping %s, persisting do db...";
        final var msgFormatted = msgRaw.formatted(scraped.size(), url);
        log.info(msgFormatted);

        return euroMarketRateWriteSupplier.apply(scraped);
    }

}
