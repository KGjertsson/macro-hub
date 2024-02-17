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
public class ExchangeRateScrapeService {

    private final ScrapeRepository scrapeRepository;

    @Value("${scrape.data.name.exchange.rate.usd-sek}")
    private String exchangeRateUsdSek;

    @Autowired
    public ExchangeRateScrapeService(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    public Integer scrapeExchangeRateUsdSek() {
        return Stream.ofNullable(exchangeRateUsdSek)
                .map(ScrapeQueueItem::of)
                .map(scrapeRepository::addScrapeQueueItem)
                .toList()
                .getFirst();
    }

}
