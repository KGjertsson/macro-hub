package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEngineGovBills;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GovernmentBillScrapeService {

    private final List<String> ALLOWED_PERIODS = List.of("1", "3", "6", "12");
    private final GovernmentBillRepository govBillRepository;
    private final ScrapeUtils scrapeUtils;

    @Autowired
    public GovernmentBillScrapeService(
            GovernmentBillRepository govBillRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.govBillRepository = govBillRepository;
        this.scrapeUtils = scrapeUtils;
    }

    public Integer scrapeGovernmentBillsSweden(String period) {
        final var errorRaw = "Expected param period to be one of [1, 3, 6, 12] but found %s";
        final var errorFormatted = errorRaw.formatted(period);

        if (!ALLOWED_PERIODS.contains(period)) {
            throw new IllegalArgumentException(errorFormatted);
        }

        return new ScrapeEngineGovBills(govBillRepository, scrapeUtils, period).scrape();

    }

}
