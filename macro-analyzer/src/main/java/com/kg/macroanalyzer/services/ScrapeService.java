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
    public ScrapeService(PolicyRateRepository policyRateRepository, ExchangeRateRepository exchangeRateRepository, GovernmentBillRepository governmentBillRepository, GovernmentBondsRepository governmentBondsRepository, EuroMarketRateRepository euroMarketRateRepository, ObjectMapper objectMapper) {
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
        final var e = ("Expected request parameter period to have one of the values [1, 3, 6, 12] " + "but found %s").formatted(period);

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
        log.info(("Found %s new 1 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills1Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills3Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB3MBENCH/1983-01-03";
        final var persisted3Month = governmentBillRepository.getSwedishGovernmentBills3Month();
        final var scraped = scrapeNovelItems(url, persisted3Month, GovernmentBillItem.class);
        log.info(("Found %s new 3 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills3Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills6Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB6MBENCH/1984-01-02";
        final var persisted6Month = governmentBillRepository.getSwedishGovernmentBills6Month();
        final var scraped = scrapeNovelItems(url, persisted6Month, GovernmentBillItem.class);
        log.info(("Found %s new 6 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills6Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills12Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB12MBENCH/1983-01-03";
        final var persisted = governmentBillRepository.getSwedishGovernmentBills12Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBillItem.class);
        log.info(("Found %s new 12 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills12Month(scraped);

        return scraped.size();
    }

    public Integer scrapeGovernmentBondsSweden(String period) throws IOException {
        final var e = ("Expected request parameter period to have one of the values [2, 5, 7, 10] " + "but found %s").formatted(period);

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
        log.info(("Found %s new 2 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds2Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBonds5Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB5YC/1985-01-02";
        final var persisted = governmentBondsRepository.getSwedishGovernmentBonds5Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info(("Found %s new 5 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds5Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBonds7Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB7YC/1987-01-02 ";
        final var persisted = governmentBondsRepository.getSwedishGovernmentBonds7Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info(("Found %s new 7 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds7Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBonds10Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB10YC/1987-01-02";
        final var persisted = governmentBondsRepository.getSwedishGovernmentBonds10Month();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info(("Found %s new 10 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
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
            default -> throw new IllegalArgumentException(e);
        };
    }

    private Integer scrapeEuroMarketRate3MonthDenmark() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MDKK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthDenmark();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthDenmark(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MEUR/1999-01-04";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthEur();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthEur(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MGBP/1979-12-19";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthGB();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthGB(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthJapan();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthNorway() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MNOK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthNorway();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthNorway(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MUSD/1979-11-28";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthUsa();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthUsa(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthDenmark() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MDKK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthDenmark();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthDenmark(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MEUR/1999-01-04";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthEur();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthEur(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MGBP/1979-11-28";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthGB();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthGB(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthJapan();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthNorway() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MNOK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthNorway();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthNorway(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MUSD/1979-11-28";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthUsa();
        final var scraped = scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthUsa(scraped);

        return scraped.size();
    }

    public Integer scrapeIntGovBonds(String periodCountry) throws IOException {
        final var e = "Found unexpected combination of query parameters country and period.";

        return switch (periodCountry) {
            case "5year-eur" -> scrapeIntGovBond5yearEur();
            case "5year-gb" -> scrapeIntGovBond5yearGB();
            case "5year-japan" -> scrapeIntGovBond5yearJapan();
            case "5year-france" -> scrapeIntGovBond5yearFrance();
            case "5year-germany" -> scrapeIntGovBond5yearGermany();
            case "5year-netherlands" -> scrapeIntGovBond5yearNetherlands();
            case "5year-usa" -> scrapeIntGovBond5yearUsa();
            case "10year-denmark" -> scrapeIntGovBond10yearDenmark();
            case "10year-eur" -> scrapeIntGovBond10yearEur();
            case "10year-finland" -> scrapeIntGovBond10yearFinland();
            case "10year-france" -> scrapeIntGovBond10yearFrance();
            case "10year-gb" -> scrapeIntGovBond10yearGB();
            case "10year-germany" -> scrapeIntGovBond10yearGermany();
            case "10year-japan" -> scrapeIntGovBond10yearJapan();
            case "10year-netherlands" -> scrapeIntGovBond10yearNetherlands();
            case "10year-norway" -> scrapeIntGovBond10yearNorway();
            case "10year-usa" -> scrapeIntGovBond10yearUsa();
            default -> throw new IllegalArgumentException(e);
        };
    }

    private Integer scrapeIntGovBond5yearGermany() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/DEGVB5Y/1987-02-09";
        final var persisted = governmentBondsRepository.getIntGovBond5YearGermany();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond5YearGermany(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearFrance() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/FRGVB5Y/1988-02-08";
        final var persisted = governmentBondsRepository.getIntGovBond5YearFrance();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond5YearFrance(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearNetherlands() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/NLGVB5Y/1987-02-09";
        final var persisted = governmentBondsRepository.getIntGovBond5YearHolland();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond5YearHolland(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EMGVB5Y/1999-01-04";
        final var persisted = governmentBondsRepository.getIntGovBond5YearEur();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond5YearEur(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/GBGVB5Y/1987-01-02";
        final var persisted = governmentBondsRepository.getIntGovBond5YearGB();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond5YearGB(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/JPGVB5Y/1987-03-23";
        final var persisted = governmentBondsRepository.getIntGovBond5YearJapan();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond5YearJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/USGVB5Y/1987-02-02";
        final var persisted = governmentBondsRepository.getIntGovBond5YearUsa();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond5YearUsa(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/USGVB10Y/1991-01-02";
        final var persisted = governmentBondsRepository.getIntGovBond10YearUsa();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearUsa(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearNorway() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/NOGVB10Y/1990-05-31";
        final var persisted = governmentBondsRepository.getIntGovBond10YearNorway();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearNorway(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearNetherlands() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/NLGVB10Y/1987-02-09";
        final var persisted = governmentBondsRepository.getIntGovBond10YearHolland();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearHolland(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/JPGVB10Y/1987-01-05";
        final var persisted = governmentBondsRepository.getIntGovBond10YearJapan();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearGermany() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/DEGVB10Y/1987-02-09";
        final var persisted = governmentBondsRepository.getIntGovBond10YearGermany();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearGermany(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/GBGVB10Y/1987-01-02";
        final var persisted = governmentBondsRepository.getIntGovBond10YearGB();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearGB(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearFrance() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/FRGVB10Y/1988-02-08";
        final var persisted = governmentBondsRepository.getIntGovBond10YearFrance();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearFrance(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearFinland() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/FIGVB10Y/1990-04-02";
        final var persisted = governmentBondsRepository.getIntGovBond10YearFinland();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearFinland(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EMGVB10Y/1999-01-04";
        final var persisted = governmentBondsRepository.getIntGovBond10YearEur();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearEur(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearDenmark() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/DKGVB10Y/1982-01-04";
        final var persisted = governmentBondsRepository.getIntGovBond10YearDenmark();
        final var scraped = scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        governmentBondsRepository.insertIntGovBond10YearDenmark(scraped);

        return scraped.size();
    }

    private <E> List<E> scrapeNovelItems(String url, List<E> persistedItems, Class<E> itemType) throws IOException {
        final var response = WebUtils.getHTTP(url);
        JavaType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
        List<E> resultList = objectMapper.readValue(response, collectionType);

        return resultList.stream().filter(i -> !persistedItems.contains(i)).toList();
    }

}
