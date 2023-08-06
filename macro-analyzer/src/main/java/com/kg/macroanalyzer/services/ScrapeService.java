package com.kg.macroanalyzer.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kg.macroanalyzer.models.exchangerate.ExchangeRateUsdSek;
import com.kg.macroanalyzer.models.governmentbills.GovernmentBillItem;
import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import com.kg.macroanalyzer.repositories.ExchangeRateRepository;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScrapeService {

    private final PolicyRateRepository policyRateRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final GovernmentBillRepository governmentBillRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ScrapeService(
            PolicyRateRepository policyRateRepository,
            ExchangeRateRepository exchangeRateRepository,
            GovernmentBillRepository governmentBillRepository,
            ObjectMapper objectMapper
    ) {
        this.policyRateRepository = policyRateRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.governmentBillRepository = governmentBillRepository;
        this.objectMapper = objectMapper;
    }

    public List<PolicyRateItem> scrapePolicyRateSweden() throws IOException {
        String endpointUrl = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";

        final var response = WebUtils.getHTTP(endpointUrl);
        final var scrapeType = new TypeReference<List<PolicyRateItem>>() {
        };
        final var persistedPolicyRateItems = policyRateRepository.getPolicyRateSweden();
        final var novelScrapedItems = objectMapper.readValue(response, scrapeType)
                .stream()
                .filter(i -> !persistedPolicyRateItems.contains(i))
                .toList();
        policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
        log.info("Found %s new items from scraping, persisting do db...".formatted(novelScrapedItems.size()));

        return novelScrapedItems;
    }

    public List<ExchangeRateUsdSek> scrapeExchangeRateUsdSek() throws IOException {
        final var endpointUrl = "https://api-test.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04";
        final var response = WebUtils.getHTTP(endpointUrl);
        final var scrapeType = new TypeReference<List<ExchangeRateUsdSek>>() {
        };
        final var persistedExchangeUsdSek = exchangeRateRepository.getExchangeRateUsdSek();
        final var novelScrapedItems = objectMapper.readValue(response, scrapeType)
                .stream()
                .filter(i -> !persistedExchangeUsdSek.contains(i))
                .toList();
        exchangeRateRepository.insertExchangeRateUsdSek(novelScrapedItems);
        log.info("Found %s new items from scraping, persisting do db...".formatted(novelScrapedItems.size()));

        return novelScrapedItems;
    }

    public List<GovernmentBillItem> scrapeGovernmentBillsSweden() throws IOException {
        final var scrapeType = new TypeReference<List<GovernmentBillItem>>() {
        };

        final var url1Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB1MBENCHC/1983-01-03";
        final var url3Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB3MBENCH/1983-01-03";
        final var url6Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB6MBENCH/1984-01-02";
        final var url12Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB12MBENCH/1983-01-03";

        final var response1Month = WebUtils.getHTTP(url1Month);
        final var response3Month = WebUtils.getHTTP(url3Month);
        final var response6Month = WebUtils.getHTTP(url6Month);
        final var response12Month = WebUtils.getHTTP(url12Month);

        final var persisted1Month = governmentBillRepository.getSwedishGovernmentBills1Month();
        final var persisted3Month = governmentBillRepository.getSwedishGovernmentBills3Month();
        final var persisted6Month = governmentBillRepository.getSwedishGovernmentBills6Month();
        final var persisted12Month = governmentBillRepository.getSwedishGovernmentBills12Month();

        final var novelScraped1 = objectMapper.readValue(response1Month, scrapeType)
                .stream()
                .filter(i -> !persisted1Month.contains(i))
                .toList();
        final var novelScraped3 = objectMapper.readValue(response3Month, scrapeType)
                .stream()
                .filter(i -> !persisted3Month.contains(i))
                .toList();
        final var novelScraped6 = objectMapper.readValue(response6Month, scrapeType)
                .stream()
                .filter(i -> !persisted6Month.contains(i))
                .toList();
        final var novelScraped12 = objectMapper.readValue(response12Month, scrapeType)
                .stream()
                .filter(i -> !persisted12Month.contains(i))
                .toList();

        log.info("Found %s new 1 month items from scraping, persisting do db...".formatted(novelScraped1.size()));
        governmentBillRepository.insertSwedishGovernmentBills1Month(novelScraped1);

        log.info("Found %s new 3 month items from scraping, persisting do db...".formatted(novelScraped3.size()));
        governmentBillRepository.insertSwedishGovernmentBills3Month(novelScraped3);

        log.info("Found %s new 6 month items from scraping, persisting do db...".formatted(novelScraped6.size()));
        governmentBillRepository.insertSwedishGovernmentBills6Month(novelScraped6);

        log.info("Found %s new 12 month items from scraping, persisting do db...".formatted(novelScraped12.size()));
        governmentBillRepository.insertSwedishGovernmentBills12Month(novelScraped12);

        final var combinedScraped = new ArrayList<GovernmentBillItem>();
        combinedScraped.addAll(novelScraped1);
        combinedScraped.addAll(novelScraped3);
        combinedScraped.addAll(novelScraped6);
        combinedScraped.addAll(novelScraped12);

        return combinedScraped;
    }

}
