package com.kg.macroanalyzer.services;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kg.macroanalyzer.models.*;
import com.kg.macroanalyzer.repositories.*;
import com.kg.macroanalyzer.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ScrapeService {

    private final PolicyRateRepository policyRateRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final GovernmentBillRepository governmentBillRepository;
    private final GovernmentBondsRepository governmentBondsRepository;
    private final EuroMarketRateRepository euroMarketRateRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ScrapeService(
            PolicyRateRepository policyRateRepository,
            ExchangeRateRepository exchangeRateRepository,
            GovernmentBillRepository governmentBillRepository,
            GovernmentBondsRepository governmentBondsRepository,
            EuroMarketRateRepository euroMarketRateRepository,
            ObjectMapper objectMapper
    ) {
        this.policyRateRepository = policyRateRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.governmentBillRepository = governmentBillRepository;
        this.governmentBondsRepository = governmentBondsRepository;
        this.euroMarketRateRepository = euroMarketRateRepository;
        this.objectMapper = objectMapper;
    }

    public Integer scrapePolicyRateSweden() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";
        final var persistedPolicyRateItems = policyRateRepository.getPolicyRateSweden();
        final var novelScrapedItems = scrapeNovelItems(url, persistedPolicyRateItems, PolicyRateItem.class);
        policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
        log.info("Found %s new items from scraping, persisting do db...".formatted(novelScrapedItems.size()));

        return novelScrapedItems.size();
    }

    public Integer scrapeExchangeRateUsdSek() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04";
        final var persisted = exchangeRateRepository.getExchangeRateUsdSek();
        final var scraped = scrapeNovelItems(url, persisted, ExchangeRateUsdSek.class);
        exchangeRateRepository.insertExchangeRateUsdSek(scraped);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));

        return scraped.size();
    }

    public Integer scrapeGovernmentBillsSweden(String period) throws IOException {
        final var e = ("Expected request parameter period to have one of the values [1, 3, 6, 12] "
                + "but found %s").formatted(period);

        return switch (period) {
            case "1" -> scrapeSwedishGovernmentBills1Month();
            case "3" -> scrapeSwedishGovernmentBills3Month();
            case "6" -> scrapeSwedishGovernmentBills6Month();
            case "12" -> scrapeSwedishGovernmentBills12Month();
            default -> throw new IllegalArgumentException(e);
        };
    }

    private Integer scrapeSwedishGovernmentBills1Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB1MBENCHC/1983-01-03";
        final var persisted1Month = governmentBillRepository.getSwedishGovernmentBills1Month();
        final var scraped = scrapeNovelItems(url, persisted1Month, GovernmentBillItem.class);
        log.info(("Found %s new 1 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills1Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills3Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB3MBENCH/1983-01-03";
        final var persisted3Month = governmentBillRepository.getSwedishGovernmentBills3Month();
        final var scraped = scrapeNovelItems(url, persisted3Month, GovernmentBillItem.class);
        log.info(("Found %s new 3 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills3Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills6Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB6MBENCH/1984-01-02";
        final var persisted6Month = governmentBillRepository.getSwedishGovernmentBills6Month();
        final var scraped = scrapeNovelItems(url, persisted6Month, GovernmentBillItem.class);
        log.info(("Found %s new 6 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills6Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills12Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB12MBENCH/1983-01-03";
        final var persisted = governmentBillRepository.getSwedishGovernmentBills12Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBillItem.class);
        log.info(("Found %s new 12 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills12Month(scraped);

        return scraped.size();
    }

    public Integer scrapeGovernmentBondsSweden(String period) throws IOException {
        final var e = ("Expected request parameter period to have one of the values [2, 5, 7, 10] "
                + "but found %s").formatted(period);

        return switch (period) {
            case "2" -> scrapeSwedishGovernmentBonds2Month();
            case "5" -> scrapeSwedishGovernmentBonds5Month();
            case "7" -> scrapeSwedishGovernmentBonds7Month();
            case "10" -> scrapeSwedishGovernmentBonds10Month();
            default -> throw new IllegalArgumentException(e);
        };
    }

    private Integer scrapeSwedishGovernmentBonds2Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB2YC/1987-01-07";
        final var persisted = governmentBondsRepository.getSwedishGovernmentBonds2Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info(("Found %s new 2 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds2Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBonds5Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB5YC/1985-01-02";
        final var persisted = governmentBondsRepository.getSwedishGovernmentBonds5Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info(("Found %s new 5 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds5Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBonds7Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB7YC/1987-01-02 ";
        final var persisted = governmentBondsRepository.getSwedishGovernmentBonds7Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info(("Found %s new 7 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds7Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBonds10Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB10YC/1987-01-02";
        final var persisted = governmentBondsRepository.getSwedishGovernmentBonds10Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info(("Found %s new 10 month items from scraping, persisting do " +
                "db...").formatted(scraped.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds10Month(scraped);

        return scraped.size();
    }

    public Integer scrapeEuroMarketRate(String periodCountry) throws IOException {
        final var e = "Found unexpected combination of query parameters country and period.";

        return switch (periodCountry) {
            case "3month-denmark" -> scrapeEuroMarketRate3MonthDenmark();
            case "3month-eur" -> scrapeEuroMarketRate3MonthEur();
            case "3month-gb" -> scrapeEuroMarketRate3MonthGB();
            case "3month-japan" -> scrapeEuroMarketRate3MonthJapan();
            case "3month-norway" -> scrapeEuroMarketRate3MonthNorway();
            case "3month-usa" -> scrapeEuroMarketRate3MonthUsa();
            case "6month-denmark" -> scrapeEuroMarketRate6MonthDenmark();
            case "6month-eur" -> scrapeEuroMarketRate6MonthEur();
            case "6month-gb" -> scrapeEuroMarketRate6MonthGB();
            case "6month-japan" -> scrapeEuroMarketRate6MonthJapan();
            case "6month-norway" -> scrapeEuroMarketRate6MonthNorway();
            case "6month-usa" -> scrapeEuroMarketRate6MonthUsa();
            case "5year-denmark" -> scrapeEuroMarketRate5yearDenmark();
            case "5year-eur" -> scrapeEuroMarketRate5yearEur();
            case "5year-gb" -> scrapeEuroMarketRate5yearGB();
            case "5year-japan" -> scrapeEuroMarketRate5yearJapan();
            case "5year-norway" -> scrapeEuroMarketRate5yearNorway();
            case "5year-usa" -> scrapeEuroMarketRate5yearUsa();
            case "10year-denmark" -> scrapeEuroMarketRate10yearDenmark();
            case "10year-eur" -> scrapeEuroMarketRate10yearEur();
            case "10year-finland" -> scrapeEuroMarketRate10yearFinland();
            case "10year-france" -> scrapeEuroMarketRate10yearFrance();
            case "10year-gb" -> scrapeEuroMarketRate10yearGB();
            case "10year-germany" -> scrapeEuroMarketRate10yearGermany();
            case "10year-japan" -> scrapeEuroMarketRate10yearJapan();
            case "10year-netherlands" -> scrapeEuroMarketRate10yearNetherlands();
            case "10year-norway" -> scrapeEuroMarketRate10yearNorway();
            case "10year-usa" -> scrapeEuroMarketRate10yearUsa();
            default -> throw new IllegalArgumentException(e);
        };
    }

    private Integer scrapeEuroMarketRate3MonthDenmark() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MDKK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthDenmark();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db..."
                .formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthDenmark(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MEUR/1999-01-04";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthEur();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db..."
                .formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthEur(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MGBP/1979-12-19";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthGB();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db..."
                .formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthGB(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthJapan();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db..."
                .formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthNorway() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MNOK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthUsa();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db..."
                .formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthUsa(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MUSD/1979-11-28";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthUsa();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db..."
                .formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthDenmark(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthDenmark() {

    }

    private Integer scrapeEuroMarketRate6MonthEur() {

    }

    private Integer scrapeEuroMarketRate6MonthGB() {

    }

    private Integer scrapeEuroMarketRate6MonthJapan() {

    }

    private Integer scrapeEuroMarketRate6MonthNorway() {

    }

    private Integer scrapeEuroMarketRate6MonthUsa() {

    }

    private Integer scrapeEuroMarketRate5yearDenmark() {

    }

    private Integer scrapeEuroMarketRate5yearEur() {

    }

    private Integer scrapeEuroMarketRate5yearGB() {

    }

    private Integer scrapeEuroMarketRate5yearJapan() {

    }

    private Integer scrapeEuroMarketRate5yearNorway() {

    }

    private Integer scrapeEuroMarketRate5yearUsa() {

    }

    private Integer scrapeEuroMarketRate10yearUsa() {
    }

    private Integer scrapeEuroMarketRate10yearNorway() {
    }

    private Integer scrapeEuroMarketRate10yearNetherlands() {
    }

    private Integer scrapeEuroMarketRate10yearJapan() {
    }

    private Integer scrapeEuroMarketRate10yearGermany() {
    }

    private Integer scrapeEuroMarketRate10yearGB() {
    }

    private Integer scrapeEuroMarketRate10yearFrance() {
    }

    private Integer scrapeEuroMarketRate10yearFinland() {
    }

    private Integer scrapeEuroMarketRate10yearEur() {
    }

    private Integer scrapeEuroMarketRate10yearDenmark() {
    }


    private <E> List<E> scrapeNovelItems(String url, List<E> persistedItems, Class<E> itemType) throws IOException {
        final var response = WebUtils.getHTTP(url);
        JavaType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
        List<E> resultList = objectMapper.readValue(response, collectionType);

        return resultList.stream().filter(i -> !persistedItems.contains(i)).toList();
    }

}
