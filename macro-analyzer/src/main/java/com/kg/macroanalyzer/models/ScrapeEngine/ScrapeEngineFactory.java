package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.repositories.GovernmentBondsRepository;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScrapeEngineFactory {

    private final PolicyRateRepository policyRateRepository;
    private final GovernmentBillRepository govBillRepository;
    private final GovernmentBondsRepository govBondRepository;
    private final ScrapeRepository scrapeRepository;
    private final ScrapeUtils scrapeUtils;

    @Autowired
    public ScrapeEngineFactory(
            PolicyRateRepository policyRateRepository,
            GovernmentBillRepository govBillRepository,
            GovernmentBondsRepository govBondRepository,
            ScrapeRepository scrapeRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.policyRateRepository = policyRateRepository;
        this.govBillRepository = govBillRepository;
        this.govBondRepository = govBondRepository;
        this.scrapeRepository = scrapeRepository;
        this.scrapeUtils = scrapeUtils;
    }

    public ScrapeEngine createScrapeEngine(ScrapeQueueItem scrapeQueueItem) {
        final var name = scrapeQueueItem.name().trim();

        return switch (name) {
            case "policy-rate/sweden" -> new ScrapeEnginePolicyRate(
                    scrapeQueueItem,
                    policyRateRepository,
                    scrapeRepository,
                    scrapeUtils
            );
            case "government-bills/sweden?period=1" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    govBillRepository,
                    scrapeRepository,
                    scrapeUtils,
                    "1"
            );
            case "government-bills/sweden?period=3" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    govBillRepository,
                    scrapeRepository,
                    scrapeUtils,
                    "3"
            );
            case "government-bills/sweden?period=6" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    govBillRepository,
                    scrapeRepository,
                    scrapeUtils,
                    "6"
            );
            case "government-bills/sweden?period=12" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    govBillRepository,
                    scrapeRepository,
                    scrapeUtils,
                    "12"
            );
            case "government-bonds/sweden?period=2" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "2year-swe");
            case "government-bonds/sweden?period=5" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-swe");
            case "government-bonds/sweden?period=7" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "7year-swe");
            case "government-bonds/sweden?period=10" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-swe");
            case "government-bonds/international?period=5year&country=eur" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-eur");
            case "government-bonds/international?period=5year&country=gb" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-gb");
            case "government-bonds/international?period=5year&country=japan" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-japan");
            case "government-bonds/international?period=5year&country=france" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-france");
            case "government-bonds/international?period=5year&country=germany" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-germany");
            case "government-bonds/international?period=5year&country=netherlands" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-netherlands");
            case "government-bonds/international?period=5year&country=usa" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "5year-usa");
            case "government-bonds/international?period=10year&country=denmark" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-denmark");
            case "government-bonds/international?period=10year&country=eur" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-eur");
            case "government-bonds/international?period=10year&country=finland" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-finland");
            case "government-bonds/international?period=10year&country=france" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-france");
            case "government-bonds/international?period=10year&country=gb" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-gb");
            case "government-bonds/international?period=10year&country=germany" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-germany");
            case "government-bonds/international?period=10year&country=japan" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-japan");
            case "government-bonds/international?period=10year&country=netherlands" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-netherlands");
            case "government-bonds/international?period=10year&country=norway" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-norway");
            case "government-bonds/international?period=10year&country=usa" ->
                    new ScrapeEngineGovBonds(govBondRepository, scrapeUtils, "10year-usa");
            default -> {
                final var msg = "Found unexpected argument [%s]".formatted(name);

                throw new IllegalArgumentException(msg);
            }
        };
    }

}
