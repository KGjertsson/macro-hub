package com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine;

import com.kg.macroanalyzer.adaptors.database.postgres.models.GovernmentBondItem;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ScrapeEngineGovBonds extends AbstractScrapeEngine<GovernmentBondItem> {

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
    protected List<GovernmentBondItem> scrapeItems() throws IOException {
        final var existingGovBondItems = govBondReadSupplier.get();
        return ScrapeUtils.scrapeNovelItems(
                url,
                existingGovBondItems,
                GovernmentBondItem.class
        );
    }

    @Override
    protected Integer insertScrapedItems(List<GovernmentBondItem> scraped) {
        final var msgRaw = "Found %s new items from scraping %s, persisting do db...";
        final var msgFormatted = msgRaw.formatted(scraped.size(), url);
        log.info(msgFormatted);

        return govBondWriteSupplier.apply(scraped);
    }

}
