package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.GovernmentBillItem;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class GovernmentBillScrapeService {

    private final GovernmentBillRepository governmentBillRepository;
    private final ScrapeUtils scrapeUtils;

    @Autowired
    public GovernmentBillScrapeService(
            GovernmentBillRepository governmentBillRepository,
            ScrapeUtils scrapeUtils
    ) {
        this.governmentBillRepository = governmentBillRepository;
        this.scrapeUtils = scrapeUtils;
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
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted1Month, GovernmentBillItem.class);
        log.info(("Found %s new 1 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills1Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills3Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB3MBENCH/1983-01-03";
        final var persisted3Month = governmentBillRepository.getSwedishGovernmentBills3Month();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted3Month, GovernmentBillItem.class);
        log.info(("Found %s new 3 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills3Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills6Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB6MBENCH/1984-01-02";
        final var persisted6Month = governmentBillRepository.getSwedishGovernmentBills6Month();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted6Month, GovernmentBillItem.class);
        log.info(("Found %s new 6 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills6Month(scraped);

        return scraped.size();
    }

    private Integer scrapeSwedishGovernmentBills12Month() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SETB12MBENCH/1983-01-03";
        final var persisted = governmentBillRepository.getSwedishGovernmentBills12Month();
        final var scraped = scrapeUtils.scrapeNovelItems(url, persisted, GovernmentBillItem.class);
        log.info(("Found %s new 12 month items from scraping, persisting do " + "db...").formatted(scraped.size()));
        governmentBillRepository.insertSwedishGovernmentBills12Month(scraped);

        return scraped.size();
    }

}
