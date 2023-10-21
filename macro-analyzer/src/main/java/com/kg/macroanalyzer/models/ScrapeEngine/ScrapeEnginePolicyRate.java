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
public class ScrapeEnginePolicyRate extends AbstractScrapeEngine<PolicyRateItem> {

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
    protected List<PolicyRateItem> scrapeItems() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";
        final var items = policyRateRepository.getPolicyRateSweden();

        return ScrapeUtils.scrapeNovelItems(
                url,
                items,
                PolicyRateItem.class
        );
    }

    @Override
    protected Integer insertScrapedItems(List<PolicyRateItem> novelScrapedItems) {
        final var msgRaw = "Found %s new items from scraping, persisting do db...";
        final var msgFormatted = msgRaw.formatted(novelScrapedItems.size());
        log.info(msgFormatted);

        return policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
    }

}
