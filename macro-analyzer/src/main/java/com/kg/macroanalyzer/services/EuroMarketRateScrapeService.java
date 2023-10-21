package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class EuroMarketRateScrapeService {

    private final ScrapeRepository scrapeRepository;
    @Value("${scrape.data.name.euro.market.rate.denmark.3month}")
    private String euroMarketRateDenmark3Month;
    @Value("${scrape.data.name.euro.market.rate.eur.3month}")
    private String euroMarketRateEur3Month;
    @Value("${scrape.data.name.euro.market.rate.gb.3month}")
    private String euroMarketRateGb3Month;
    @Value("${scrape.data.name.euro.market.rate.japan.3month}")
    private String euroMarketRateJapan3Month;
    @Value("${scrape.data.name.euro.market.rate.norway.3month}")
    private String euroMarketRateNorway3Month;
    @Value("${scrape.data.name.euro.market.rate.usa.3month}")
    private String euroMarketRateUsa3Month;
    @Value("${scrape.data.name.euro.market.rate.denmark.6month}")
    private String euroMarketRateDenmark6Month;
    @Value("${scrape.data.name.euro.market.rate.eur.6month}")
    private String euroMarketRateEur6Month;
    @Value("${scrape.data.name.euro.market.rate.gb.6month}")
    private String euroMarketRateGb6Month;
    @Value("${scrape.data.name.euro.market.rate.japan.6month}")
    private String euroMarketRateJapan6Month;
    @Value("${scrape.data.name.euro.market.rate.norway.6month}")
    private String euroMarketRateNorway6Month;
    @Value("${scrape.data.name.euro.market.rate.usa.6month}")
    private String euroMarketRateUsa6Month;

    public EuroMarketRateScrapeService(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    public Integer scrapeEuroMarketRate(String periodCountry) {
        return Stream.ofNullable(periodCountry)
                .map(this::periodCountryToName)
                .map(ScrapeQueueItem::of)
                .map(scrapeRepository::addScrapeQueueItem)
                .toList()
                .getFirst();
    }

    private String periodCountryToName(String periodCountry) {
        final var e = "Found unexpected combination of query parameters country and period.";

        return switch (periodCountry) {
            case "3month-denmark" -> euroMarketRateDenmark3Month;
            case "3month-eur" -> euroMarketRateEur3Month;
            case "3month-gb" -> euroMarketRateGb3Month;
            case "3month-japan" -> euroMarketRateJapan3Month;
            case "3month-norway" -> euroMarketRateNorway3Month;
            case "3month-usa" -> euroMarketRateUsa3Month;
            case "6month-denmark" -> euroMarketRateDenmark6Month;
            case "6month-eur" -> euroMarketRateEur6Month;
            case "6month-gb" -> euroMarketRateGb6Month;
            case "6month-japan" -> euroMarketRateJapan6Month;
            case "6month-norway" -> euroMarketRateNorway6Month;
            case "6month-usa" -> euroMarketRateUsa6Month;
            default -> throw new IllegalArgumentException(e);
        };
    }

}
