package com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ScrapeEnginePolicyRate extends AbstractScrapeEngine<MacroPoint> {

    private final PolicyRateRepository policyRateRepository;

    public ScrapeEnginePolicyRate(
            ScrapeQueueItem scrapeQueueItem,
            PolicyRateRepository policyRateRepository,
            ScrapeRepository scrapeRepository
    ) {
        super(scrapeRepository, scrapeQueueItem);
        this.policyRateRepository = policyRateRepository;
    }

    @Override
    protected List<MacroPoint> scrapeItems() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";
        final var items = policyRateRepository.policyRateSwedenReader().get();

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

        return policyRateRepository.insertPolicyRateItemsSweden().apply(novelScrapedItems);
    }

}
