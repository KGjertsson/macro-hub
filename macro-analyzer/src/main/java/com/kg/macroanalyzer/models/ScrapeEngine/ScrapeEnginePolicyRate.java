package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.PolicyRateItem;
import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ScrapeEnginePolicyRate extends AbstractScrapeEngine {

    private final PolicyRateRepository policyRateRepository;
    private final ScrapeUtils scrapeUtils;

    public ScrapeEnginePolicyRate(
            ScrapeQueueItem scrapeQueueItem,
            PolicyRateRepository policyRateRepository,
            ScrapeRepository scrapeRepository,
            ScrapeUtils scrapeUtils
    ) {
        super(scrapeRepository, scrapeQueueItem);
        this.policyRateRepository = policyRateRepository;
        this.scrapeUtils = scrapeUtils;
    }

    @Override
    public Integer scrape() {
        try {
            final var novelScrapedItems = scrapeItems();
            insertScrapedItems(novelScrapedItems);
            this.markAsDone();

            return novelScrapedItems.size();
        } catch (IOException ioException) {
            final var msgRaw = "Received IOException while scraping sweden policy rate: %s";
            final var msgFormatted = msgRaw.formatted(ioException.getMessage());
            log.error(msgFormatted);

            return 0;
        }
    }

    private List<PolicyRateItem> scrapeItems() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";
        final var items = policyRateRepository.getPolicyRateSweden();
        return scrapeUtils.scrapeNovelItems(
                url,
                items,
                PolicyRateItem.class
        );
    }

    private void insertScrapedItems(List<PolicyRateItem> novelScrapedItems) {
        policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
        final var msgRaw = "Found %s new items from scraping, persisting do db...";
        final var msgFormatted = msgRaw.formatted(novelScrapedItems.size());
        log.info(msgFormatted);
    }

}
