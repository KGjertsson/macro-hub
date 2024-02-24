package com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ScrapeEngineGovBills extends AbstractScrapeEngine<MacroPoint> {

    private final String url;
    private final Supplier<List<MacroPoint>> govBillReadSupplier;
    private final Function<List<MacroPoint>, Integer> govBillWriteSupplier;

    public ScrapeEngineGovBills(
            ScrapeQueueItem scrapeQueueItem,
            ScrapeRepository scrapeRepository,
            String url,
            Supplier<List<MacroPoint>> govBillReadSupplier,
            Function<List<MacroPoint>, Integer> govBillWriteSupplier

    ) {
        super(scrapeRepository, scrapeQueueItem);
        final var baseUrl = "https://api-test.riksbank.se/swea/v1/Observations";

        this.url = baseUrl + url;
        this.govBillReadSupplier = govBillReadSupplier;
        this.govBillWriteSupplier = govBillWriteSupplier;
    }

    @Override
    protected List<MacroPoint> scrapeItems() throws IOException {
        final var existingGovBillItems = govBillReadSupplier.get();
        return ScrapeUtils.scrapeNovelItems(
                url,
                existingGovBillItems,
                MacroPoint.class
        );
    }

    @Override
    protected Integer insertScrapedItems(List<MacroPoint> scraped) {
        final var msgRaw = "Found %s new items from scraping %s, persisting do db...";
        final var msgFormatted = msgRaw.formatted(scraped.size(), url);
        log.info(msgFormatted);

        return govBillWriteSupplier.apply(scraped);
    }

}
