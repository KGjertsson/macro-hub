package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.GovernmentBondItem;
import com.kg.macroanalyzer.repositories.GovernmentBondsRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ScrapeEngineGovBondsSweden implements ScrapeEngine {

    private final ScrapeUtils scrapeUtils;
    private final String url;
    private final Supplier<List<GovernmentBondItem>> govBondReadSupplier;
    private final Function<List<GovernmentBondItem>, Integer> govBondWriteSupplier;

    public ScrapeEngineGovBondsSweden(
            GovernmentBondsRepository govBondsRepository,
            ScrapeUtils scrapeUtils,
            String period
    ) {
        this.scrapeUtils = scrapeUtils;
        final var baseUrl = "https://api-test.riksbank.se/swea/v1/Observations";

        switch (period) {
            case "2" -> {
                this.url = baseUrl + "/SEGVB2YC/1987-01-07";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond2YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond2YearWriter();
            }
            case "5" -> {
                this.url = baseUrl + "/SEGVB5YC/1985-01-02";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond5YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond5YearWriter();
            }
            case "7" -> {
                this.url = baseUrl + "/SEGVB7YC/1987-01-02";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond7YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond7YearWriter();
            }
            case "10" -> {
                this.url = baseUrl + "SEGVB10YC/1987-01-02";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond10YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond10YearWriter();
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public Integer scrape() {
        try {
            final var existingGovBondItems = govBondReadSupplier.get();
            final var scraped = scrapeUtils.scrapeNovelItems(
                    url,
                    existingGovBondItems,
                    GovernmentBondItem.class
            );
            final var msgRaw = "Found %s new items from scraping %s, persisting do db...";
            final var msgFormatted = msgRaw.formatted(url, scraped.size());
            log.info(msgFormatted);

            return govBondWriteSupplier.apply(scraped);
        } catch (IOException ioException) {

            return 0;
        }
    }

}
