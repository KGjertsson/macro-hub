package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.PolicyRateItem;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class PolicyRateScrapeService {

    private final PolicyRateRepository policyRateRepository;
    private final ScrapeUtils scrapeUtils;

    @Autowired
    public PolicyRateScrapeService(
            PolicyRateRepository policyRateRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.policyRateRepository = policyRateRepository;
        this.scrapeUtils = scrapeUtils;
    }

    public Integer scrapePolicyRateSweden() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";
        final var persistedPolicyRateItems = policyRateRepository.getPolicyRateSweden();
        final var novelScrapedItems = scrapeUtils.scrapeNovelItems(url, persistedPolicyRateItems, PolicyRateItem.class);
        policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
        log.info("Found %s new items from scraping, persisting do db...".formatted(novelScrapedItems.size()));

        return novelScrapedItems.size();
    }

}
