package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.PolicyRateItem;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ScrapeEnginePolicyRate implements ScrapeEngine {

    private final PolicyRateRepository policyRateRepository;
    private final ScrapeUtils scrapeUtils;

    public ScrapeEnginePolicyRate(
            PolicyRateRepository policyRateRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.policyRateRepository = policyRateRepository;
        this.scrapeUtils = scrapeUtils;
    }

    @Override
    public Integer scrape() {
        try {
            String url = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";
            final var items = policyRateRepository.getPolicyRateSweden();
            final var novelScrapedItems = scrapeUtils.scrapeNovelItems(
                    url,
                    items,
                    PolicyRateItem.class
            );
            policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
            final var msgRaw = "Found %s new items from scraping, persisting do db...";
            final var msgFormatted = msgRaw.formatted(novelScrapedItems.size());
            log.info(msgFormatted);

            return novelScrapedItems.size();
        } catch (IOException ioException) {
            final var msgRaw = "Received IOException while scraping sweden policy rate: %s";
            final var msgFormatted = msgRaw.formatted(ioException.getMessage());
            log.error(msgFormatted);

            return 0;
        }
    }

}
