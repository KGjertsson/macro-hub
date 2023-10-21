package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.GovernmentBondItem;
import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ScrapeEngineGovBonds extends AbstractScrapeEngine {

    private final String url;
    private final Supplier<List<GovernmentBondItem>> govBondReadSupplier;
    private final Function<List<GovernmentBondItem>, Integer> govBondWriteSupplier;

    public ScrapeEngineGovBonds(
            ScrapeQueueItem scrapeQueueItem,
            ScrapeRepository scrapeRepository,
            String url,
            Supplier<List<GovernmentBondItem>> govBondReadSupplier,
            Function<List<GovernmentBondItem>, Integer> govBondWriteSupplier
    ) {
        super(scrapeRepository, scrapeQueueItem);
        final var baseUrl = "https://api-test.riksbank.se/swea/v1/Observations";

        this.url = baseUrl + url;
        this.govBondReadSupplier = govBondReadSupplier;
        this.govBondWriteSupplier = govBondWriteSupplier;
    }

    @Override
    public Integer scrape() {
        try {
            final var scraped = scrapeItems();
            final var inserted = insertScrapedItems(scraped);
            this.markAsDone();

            return inserted;
        } catch (IOException | RuntimeException e) {
            log.error("Exception while attempting to scrape data: %s".formatted(e.getMessage()));

            return 0;
        }
    }

    private List<GovernmentBondItem> scrapeItems() throws IOException {
        final var existingGovBondItems = govBondReadSupplier.get();
        return ScrapeUtils.scrapeNovelItems(
                url,
                existingGovBondItems,
                GovernmentBondItem.class
        );
    }

    private Integer insertScrapedItems(List<GovernmentBondItem> scraped) {
        final var msgRaw = "Found %s new items from scraping %s, persisting do db...";
        final var msgFormatted = msgRaw.formatted(scraped.size(), url);
        log.info(msgFormatted);

        return govBondWriteSupplier.apply(scraped);
    }

}
