package com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine;

import com.kg.macroanalyzer.adaptors.database.postgres.models.GovernmentBillItem;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ScrapeEngineGovBills extends AbstractScrapeEngine<GovernmentBillItem> {

    private final String url;
    private final Supplier<List<GovernmentBillItem>> govBillReadSupplier;
    private final Function<List<GovernmentBillItem>, Integer> govBillWriteSupplier;

    public ScrapeEngineGovBills(
            ScrapeQueueItem scrapeQueueItem,
            ScrapeRepository scrapeRepository,
            String url,
            Supplier<List<GovernmentBillItem>> govBillReadSupplier,
            Function<List<GovernmentBillItem>, Integer> govBillWriteSupplier

    ) {
        super(scrapeRepository, scrapeQueueItem);
        final var baseUrl = "https://api-test.riksbank.se/swea/v1/Observations";

        this.url = baseUrl + url;
        this.govBillReadSupplier = govBillReadSupplier;
        this.govBillWriteSupplier = govBillWriteSupplier;
    }

    @Override
    protected List<GovernmentBillItem> scrapeItems() throws IOException {
        final var existingGovBillItems = govBillReadSupplier.get();
        return ScrapeUtils.scrapeNovelItems(
                url,
                existingGovBillItems,
                GovernmentBillItem.class
        );
    }

    @Override
    protected Integer insertScrapedItems(List<GovernmentBillItem> scraped) {
        final var msgRaw = "Found %s new items from scraping %s, persisting do db...";
        final var msgFormatted = msgRaw.formatted(scraped.size(), url);
        log.info(msgFormatted);

        return govBillWriteSupplier.apply(scraped);
    }

}
