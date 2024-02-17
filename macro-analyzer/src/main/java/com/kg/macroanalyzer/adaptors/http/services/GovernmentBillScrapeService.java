package com.kg.macroanalyzer.adaptors.http.services;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class GovernmentBillScrapeService {

    private final ScrapeRepository scrapeRepository;
    @Value("${scrape.data.name.gov.bills.sweden.1}")
    private String govBillSwedenName1;
    @Value("${scrape.data.name.gov.bills.sweden.3}")
    private String govBillSwedenName3;
    @Value("${scrape.data.name.gov.bills.sweden.6}")
    private String govBillSwedenName6;
    @Value("${scrape.data.name.gov.bills.sweden.12}")
    private String govBillSwedenName12;

    @Autowired
    public GovernmentBillScrapeService(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    public Integer scrapeGovernmentBillsSweden(String period) {
        return Stream.ofNullable(period)
                .map(this::periodToName)
                .map(ScrapeQueueItem::of)
                .map(scrapeRepository::addScrapeQueueItem)
                .toList()
                .getFirst();
    }

    private String periodToName(String period) {
        final var errorRaw = "Expected param period to be one of [1, 3, 6, 12] but found %s";
        final var errorFormatted = errorRaw.formatted(period);

        return switch (period) {
            case "1" -> govBillSwedenName1;
            case "3" -> govBillSwedenName3;
            case "6" -> govBillSwedenName6;
            case "12" -> govBillSwedenName12;
            default -> throw new IllegalArgumentException(errorFormatted);
        };
    }

}
