package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.GovernmentBillItem;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ScrapeEngineGovBill implements ScrapeEngine {

    private final ScrapeUtils scrapeUtils;
    private final String url;
    private final Supplier<List<GovernmentBillItem>> govBillReadSupplier;
    private final Function<List<GovernmentBillItem>, Integer> govBillWriteSupplier;

    public ScrapeEngineGovBill(
            GovernmentBillRepository govBillRepository,
            ScrapeUtils scrapeUtils,
            String period
    ) {
        this.scrapeUtils = scrapeUtils;
        final var baseUrl = "https://api-test.riksbank.se/swea/v1/Observations";

        switch (period) {
            case "1" -> {
                this.url = baseUrl + "/SETB1MBENCHC/1983-01-03";
                this.govBillReadSupplier = govBillRepository.swedishGovBills1MonthReader();
                this.govBillWriteSupplier = govBillRepository.swedishGovBill1MonthWriter();
            }
            case "3" -> {
                this.url = baseUrl + "/SETB3MBENCH/1983-01-03";
                this.govBillReadSupplier = govBillRepository.swedishGovBills3MonthReader();
                this.govBillWriteSupplier = govBillRepository.swedishGovBill3MonthWriter();
            }
            case "6" -> {
                this.url = baseUrl + "/SETB6MBENCH/1984-01-02";
                this.govBillReadSupplier = govBillRepository.swedishGovBills6MonthReader();
                this.govBillWriteSupplier = govBillRepository.swedishGovBill6MonthWriter();
            }
            case "12" -> {
                this.url = baseUrl + "/SETB12MBENCH/1983-01-03";
                this.govBillReadSupplier = govBillRepository.swedishGovBills12MonthReader();
                this.govBillWriteSupplier = govBillRepository.swedishGovBill12MonthWriter();
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public Integer scrape() {
        try {
            final var existingGovBillItems = govBillReadSupplier.get();
            final var scraped = scrapeUtils.scrapeNovelItems(
                    url,
                    existingGovBillItems,
                    GovernmentBillItem.class
            );
            final var msgRaw = "Found %s new items from scraping %s, persisting do db...";
            final var msgFormatted = msgRaw.formatted(url, scraped.size());
            log.info(msgFormatted);

            return govBillWriteSupplier.apply(scraped);
        } catch (IOException ioException) {
            final var msgRaw = "Received IOException while scraping gov bill items: %s";
            final var msgFormatted = msgRaw.formatted(ioException.getMessage());
            log.error(msgFormatted);

            return 0;
        }
    }


}
