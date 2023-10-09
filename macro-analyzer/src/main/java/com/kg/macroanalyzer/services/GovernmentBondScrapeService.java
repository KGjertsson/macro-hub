package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.GovernmentBondItem;
import com.kg.macroanalyzer.models.ScrapeEngine.ScrapeEngineGovBondsSweden;
import com.kg.macroanalyzer.repositories.GovernmentBondsRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class GovernmentBondScrapeService {

    private final List<String> ALLOWED_PERIODS = List.of("2", "5", "7", "10");
    private final GovernmentBondsRepository govBondsRepository;
    private final ScrapeUtils scrapeUtils;


    public GovernmentBondScrapeService(
            GovernmentBondsRepository govBondsRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.govBondsRepository = govBondsRepository;
        this.scrapeUtils = scrapeUtils;
    }

    public Integer scrapeGovernmentBondsSweden(String period) {
        final var errorRaw = "Expected param period to be one of [2, 5, 7, 10] but found %s";
        final var errorFormatted = errorRaw.formatted(period);

        if (!ALLOWED_PERIODS.contains(period)) {
            throw new IllegalArgumentException(errorFormatted);
        }

        return new ScrapeEngineGovBondsSweden(govBondsRepository, scrapeUtils, period).scrape();
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
        final var persisted = govBondsRepository.getIntGovBond5YearGermany();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond5YearGermany(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearFrance() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/FRGVB5Y/1988-02-08";
        final var persisted = govBondsRepository.getIntGovBond5YearFrance();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond5YearFrance(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearNetherlands() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/NLGVB5Y/1987-02-09";
        final var persisted = govBondsRepository.getIntGovBond5YearHolland();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond5YearHolland(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EMGVB5Y/1999-01-04";
        final var persisted = govBondsRepository.getIntGovBond5YearEur();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond5YearEur(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/GBGVB5Y/1987-01-02";
        final var persisted = govBondsRepository.getIntGovBond5YearGB();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond5YearGB(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/JPGVB5Y/1987-03-23";
        final var persisted = govBondsRepository.getIntGovBond5YearJapan();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond5YearJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond5yearUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/USGVB5Y/1987-02-02";
        final var persisted = govBondsRepository.getIntGovBond5YearUsa();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond5YearUsa(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/USGVB10Y/1991-01-02";
        final var persisted = govBondsRepository.getIntGovBond10YearUsa();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearUsa(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearNorway() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/NOGVB10Y/1990-05-31";
        final var persisted = govBondsRepository.getIntGovBond10YearNorway();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearNorway(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearNetherlands() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/NLGVB10Y/1987-02-09";
        final var persisted = govBondsRepository.getIntGovBond10YearHolland();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearHolland(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/JPGVB10Y/1987-01-05";
        final var persisted = govBondsRepository.getIntGovBond10YearJapan();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearGermany() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/DEGVB10Y/1987-02-09";
        final var persisted = govBondsRepository.getIntGovBond10YearGermany();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearGermany(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/GBGVB10Y/1987-01-02";
        final var persisted = govBondsRepository.getIntGovBond10YearGB();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearGB(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearFrance() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/FRGVB10Y/1988-02-08";
        final var persisted = govBondsRepository.getIntGovBond10YearFrance();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearFrance(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearFinland() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/FIGVB10Y/1990-04-02";
        final var persisted = govBondsRepository.getIntGovBond10YearFinland();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearFinland(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EMGVB10Y/1999-01-04";
        final var persisted = govBondsRepository.getIntGovBond10YearEur();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearEur(scraped);

        return scraped.size();
    }

    private Integer scrapeIntGovBond10yearDenmark() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/DKGVB10Y/1982-01-04";
        final var persisted = govBondsRepository.getIntGovBond10YearDenmark();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBondItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        govBondsRepository.insertIntGovBond10YearDenmark(scraped);

        return scraped.size();
    }

}
