package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.EuroMarketRateItem;
import com.kg.macroanalyzer.repositories.EuroMarketRateRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class EuroMarketRateScrapeService {

    private final EuroMarketRateRepository euroMarketRateRepository;
    private final ScrapeUtils scrapeUtils;

    public EuroMarketRateScrapeService(
            EuroMarketRateRepository euroMarketRateRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.euroMarketRateRepository = euroMarketRateRepository;
        this.scrapeUtils = scrapeUtils;
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
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthDenmark(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MEUR/1999-01-04";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthEur();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthEur(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MGBP/1979-12-19";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthGB();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthGB(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthJapan();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthNorway() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MNOK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthNorway();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthNorway(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate3MonthUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MUSD/1979-11-28";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthUsa();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate3MonthUsa(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthDenmark() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MDKK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthDenmark();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthDenmark(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthEur() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MEUR/1999-01-04";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthEur();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthEur(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthGB() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MGBP/1979-11-28";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthGB();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthGB(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthJapan() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP3MJPY/1979-11-29";
        final var persisted = euroMarketRateRepository.getEuroMarketRate3MonthJapan();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthJapan(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthNorway() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MNOK/1981-11-12";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthNorway();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthNorway(scraped);

        return scraped.size();
    }

    private Integer scrapeEuroMarketRate6MonthUsa() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/EUDP6MUSD/1979-11-28";
        final var persisted = euroMarketRateRepository.getEuroMarketRate6MonthUsa();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, EuroMarketRateItem.class);
        log.info("Found %s new items from scraping, persisting do db...".formatted(scraped.size()));
        euroMarketRateRepository.insertEuroMarketRate6MonthUsa(scraped);

        return scraped.size();
    }

}
