package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScrapeEngineFactory {

    private final PolicyRateRepository policyRateRepository;
    private final GovernmentBillRepository govBillRepository;
    private final ScrapeUtils scrapeUtils;

    @Autowired
    public ScrapeEngineFactory(
            PolicyRateRepository policyRateRepository,
            GovernmentBillRepository govBillRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.policyRateRepository = policyRateRepository;
        this.govBillRepository = govBillRepository;
        this.scrapeUtils = scrapeUtils;
    }

    public ScrapeEngine createScrapeEngine(ScrapeQueueItem scrapeQueueItem) {
        return switch (scrapeQueueItem.name()) {
            case "policy-rate/sweden" ->
                    new ScrapeEnginePolicyRate(policyRateRepository, scrapeUtils);
            case "government-bills/sweden?period=1" ->
                    new ScrapeEngineGovernmentBill(govBillRepository, scrapeUtils, "1");
            case "government-bills/sweden?period=3" ->
                    new ScrapeEngineGovernmentBill(govBillRepository, scrapeUtils, "3");
            case "government-bills/sweden?period=6" ->
                    new ScrapeEngineGovernmentBill(govBillRepository, scrapeUtils, "6");
            case "government-bills/sweden?period=12" ->
                    new ScrapeEngineGovernmentBill(govBillRepository, scrapeUtils, "12");
            default -> throw new IllegalArgumentException();
        };
    }

}
