package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class GovernmentBondScrapeService {

    private final ScrapeRepository scrapeRepository;

    @Value("${scrape.data.name.gov.bonds.sweden.2year}")
    private String govBondSweden2Year;
    @Value("${scrape.data.name.gov.bonds.sweden.5year}")
    private String govBondSweden5Year;
    @Value("${scrape.data.name.gov.bonds.sweden.7year}")
    private String govBondSweden7Year;
    @Value("${scrape.data.name.gov.bonds.sweden.10year}")
    private String govBondSweden10Year;
    @Value("${scrape.data.name.gov.bonds.gb.5year}")
    private String govBondGb5Year;
    @Value("${scrape.data.name.gov.bonds.gb.10year}")
    private String govBondGb10Year;
    @Value("${scrape.data.name.gov.bonds.eur.5year}")
    private String govBondEur5Year;
    @Value("${scrape.data.name.gov.bonds.eur.10year}")
    private String govBondEur10Year;
    @Value("${scrape.data.name.gov.bonds.japan.5year}")
    private String govBondJapan5Year;
    @Value("${scrape.data.name.gov.bonds.japan.10year}")
    private String govBondJapan10Year;
    @Value("${scrape.data.name.gov.bonds.france.5year}")
    private String govBondFrance5Year;
    @Value("${scrape.data.name.gov.bonds.france.10year}")
    private String govBondFrance10Year;
    @Value("${scrape.data.name.gov.bonds.germany.5year}")
    private String govBondGermany5Year;
    @Value("${scrape.data.name.gov.bonds.germany.10year}")
    private String govBondGermany10Year;
    @Value("${scrape.data.name.gov.bonds.netherlands.5year}")
    private String govBondNetherlands5Year;
    @Value("${scrape.data.name.gov.bonds.netherlands.10year}")
    private String govBondNetherlands10Year;
    @Value("${scrape.data.name.gov.bonds.usa.5year}")
    private String govBondUsa5Year;
    @Value("${scrape.data.name.gov.bonds.usa.10year}")
    private String govBondUsa10Year;
    @Value("${scrape.data.name.gov.bonds.denmark.10year}")
    private String govBondDenmark10Year;
    @Value("${scrape.data.name.gov.bonds.finland.10year}")
    private String govBondFinland10Year;
    @Value("${scrape.data.name.gov.bonds.norway.10year}")
    private String govBondNorway10Year;

    @Autowired
    public GovernmentBondScrapeService(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    public Integer scrapeGovBonds(String periodCountry) {
        return Stream.ofNullable(periodCountry)
                .map(this::periodCountryToName)
                .map(ScrapeQueueItem::of)
                .map(scrapeRepository::addScrapeQueueItem)
                .toList()
                .getFirst();
    }

    private String periodCountryToName(String periodCountry) {
        final var errorRaw = "Unexpected value of param periodCountry: %s";
        final var errorFormatted = errorRaw.formatted(periodCountry);

        return switch (periodCountry) {
            case "2year-swe" -> govBondSweden2Year;
            case "5year-swe" -> govBondSweden5Year;
            case "7year-swe" -> govBondSweden7Year;
            case "10year-swe" -> govBondSweden10Year;
            case "5year-gb" -> govBondGb5Year;
            case "10year-gb" -> govBondGb10Year;
            case "5year-eur" -> govBondEur5Year;
            case "10year-eur" -> govBondEur10Year;
            case "5year-japan" -> govBondJapan5Year;
            case "10year-japan" -> govBondJapan10Year;
            case "5year-france" -> govBondFrance5Year;
            case "10year-france" -> govBondFrance10Year;
            case "5year-germany" -> govBondGermany5Year;
            case "10year-germany" -> govBondGermany10Year;
            case "5year-netherlands" -> govBondNetherlands5Year;
            case "10year-netherlands" -> govBondNetherlands10Year;
            case "5year-usa" -> govBondUsa5Year;
            case "10year-usa" -> govBondUsa10Year;
            case "10year-denmark" -> govBondDenmark10Year;
            case "10year-finland" -> govBondFinland10Year;
            case "10year-norway" -> govBondNorway10Year;
            default -> throw new IllegalArgumentException(errorFormatted);
        };
    }

}
