package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEngineGovBonds;
import com.kg.macroanalyzer.repositories.GovernmentBondsRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GovernmentBondScrapeService {

    private final List<String> ALLOWED_PERIOD_COUNTRIES = List.of(
            "2year-swe",
            "5year-swe",
            "5year-eur",
            "5year-gb",
            "5year-japan",
            "5year-france",
            "5year-germany",
            "5year-netherlands",
            "5year-usa",
            "7year-swe",
            "10year-swe",
            "10year-denmark",
            "10year-eur",
            "10year-finland",
            "10year-france",
            "10year-gb",
            "10year-germany",
            "10year-japan",
            "10year-netherlands",
            "10year-norway",
            "10year-usa"
    );

    private final GovernmentBondsRepository govBondsRepository;
    private final ScrapeUtils scrapeUtils;


    public GovernmentBondScrapeService(
            GovernmentBondsRepository govBondsRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.govBondsRepository = govBondsRepository;
        this.scrapeUtils = scrapeUtils;
    }

    public Integer scrapeGovBonds(String periodCountry) {
        final var errorRaw = "Expected param period to be one of %S but found %s";
        final var errorFormatted = errorRaw.formatted(ALLOWED_PERIOD_COUNTRIES, periodCountry);

        if (!ALLOWED_PERIOD_COUNTRIES.contains(periodCountry)) {
            throw new IllegalArgumentException(errorFormatted);
        }

        return new ScrapeEngineGovBonds(govBondsRepository, scrapeUtils, periodCountry).scrape();
    }

}
