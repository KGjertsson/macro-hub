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
public class ScrapeEngineGovBonds implements ScrapeEngine {

    private final ScrapeUtils scrapeUtils;
    private final String url;
    private final Supplier<List<GovernmentBondItem>> govBondReadSupplier;
    private final Function<List<GovernmentBondItem>, Integer> govBondWriteSupplier;

    public ScrapeEngineGovBonds(
            GovernmentBondsRepository govBondsRepository,
            ScrapeUtils scrapeUtils,
            String periodCountry
    ) {
        this.scrapeUtils = scrapeUtils;
        final var baseUrl = "https://api-test.riksbank.se/swea/v1/Observations";

        switch (periodCountry) {
            case "2year-swe" -> {
                this.url = baseUrl + "/SEGVB2YC/1987-01-07";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond2YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond2YearWriter();
            }
            case "5year-swe" -> {
                this.url = baseUrl + "/SEGVB5YC/1985-01-02";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond5YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond5YearWriter();
            }
            case "5year-eur" -> {
                this.url = baseUrl + "/EMGVB5Y/1999-01-04";
                this.govBondReadSupplier = govBondsRepository.intGovBond5YearReaderEur();
                this.govBondWriteSupplier = govBondsRepository.intGovBond5YearWriterEur();
            }
            case "5year-gb" -> {
                this.url = baseUrl + "/GBGVB5Y/1987-01-02";
                this.govBondReadSupplier = govBondsRepository.intGovBond5YearReaderGB();
                this.govBondWriteSupplier = govBondsRepository.intGovBond5YearWriterGB();
            }
            case "5year-japan" -> {
                this.url = baseUrl + "/JPGVB5Y/1987-03-23";
                this.govBondReadSupplier = govBondsRepository.intGovBond5YearReaderJapan();
                this.govBondWriteSupplier = govBondsRepository.intGovBond5YearWriterJapan();
            }
            case "5year-france" -> {
                this.url = baseUrl + "/FRGVB5Y/1988-02-08";
                this.govBondReadSupplier = govBondsRepository.intGovBond5YearReaderFrance();
                this.govBondWriteSupplier = govBondsRepository.intGovBond5YearWriterFrance();
            }
            case "5year-germany" -> {
                this.url = baseUrl + "/DEGVB5Y/1987-02-09";
                this.govBondReadSupplier = govBondsRepository.intGovBond5YearReaderGermany();
                this.govBondWriteSupplier = govBondsRepository.intGovBond5YearWriterGermany();
            }
            case "5year-netherlands" -> {
                this.url = baseUrl + "/NLGVB5Y/1987-02-09";
                this.govBondReadSupplier = govBondsRepository.intGovBond5YearReaderHolland();
                this.govBondWriteSupplier = govBondsRepository.intGovBond5YearWriterHolland();
            }
            case "5year-usa" -> {
                this.url = baseUrl + "/USGVB5Y/1987-02-02";
                this.govBondReadSupplier = govBondsRepository.intGovBond5YearReaderUSA();
                this.govBondWriteSupplier = govBondsRepository.intGovBond5YearWriterUSA();
            }
            case "7year-swe" -> {
                this.url = baseUrl + "/SEGVB7YC/1987-01-02";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond7YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond7YearWriter();
            }
            case "10year-swe" -> {
                this.url = baseUrl + "SEGVB10YC/1987-01-02";
                this.govBondReadSupplier = govBondsRepository.swedishGovBond10YearReader();
                this.govBondWriteSupplier = govBondsRepository.swedishGovBond10YearWriter();
            }
            case "10year-denmark" -> {
                this.url = baseUrl + "/DKGVB10Y/1982-01-04";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderDenmark();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterDenmark();
            }
            case "10year-eur" -> {
                this.url = baseUrl + "/EMGVB10Y/1999-01-04";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderEur();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterEur();
            }
            case "10year-finland" -> {
                this.url = baseUrl + "/FIGVB10Y/1990-04-02";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderFinland();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterFinland();
            }
            case "10year-france" -> {
                this.url = baseUrl + "/FRGVB10Y/1988-02-08";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderFrance();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterFrance();
            }
            case "10year-gb" -> {
                this.url = baseUrl + "/GBGVB10Y/1987-01-02";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderGB();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterGB();
            }
            case "10year-germany" -> {
                this.url = baseUrl + "/DEGVB10Y/1987-02-09";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderGermany();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterGermany();
            }
            case "10year-japan" -> {
                this.url = baseUrl + "/JPGVB10Y/1987-01-05";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderJapan();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterJapan();
            }
            case "10year-netherlands" -> {
                this.url = baseUrl + "/NLGVB10Y/1987-02-09";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderHolland();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterHolland();
            }
            case "10year-norway" -> {
                this.url = baseUrl + "/NOGVB10Y/1990-05-31";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderNorway();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterNorway();
            }
            case "10year-usa" -> {
                this.url = baseUrl + "/USGVB10Y/1991-01-02";
                this.govBondReadSupplier = govBondsRepository.intGovBond10YearReaderUSA();
                this.govBondWriteSupplier = govBondsRepository.intGovBond10YearWriterUSA();
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
