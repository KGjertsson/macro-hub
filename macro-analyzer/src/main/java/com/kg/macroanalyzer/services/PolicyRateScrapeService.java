package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEnginePolicyRate;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Integer scrapePolicyRateSweden() {
        return new ScrapeEnginePolicyRate(policyRateRepository, scrapeUtils).scrape();
    }

}
