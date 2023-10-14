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
public class PolicyRateScrapeService {

    private final ScrapeRepository scrapeRepository;

    @Value("${scrape.data.name.policy.rate.sweden}")
    private String policyRateSwedenName;

    @Autowired
    public PolicyRateScrapeService(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    public Integer scrapePolicyRateSweden() {
        return Stream.of(policyRateSwedenName)
                .map(ScrapeQueueItem::of)
                .map(scrapeRepository::addScrapeQueueItem)
                .toList()
                .getFirst();
    }

}
