package com.kg.macroanalyzer.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kg.macroanalyzer.models.policyrate.PolicyRateItemSweden;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
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
    private final ObjectMapper objectMapper;

    @Autowired
    public ScrapeService(PolicyRateRepository policyRateRepository, ObjectMapper objectMapper) {
        this.policyRateRepository = policyRateRepository;
        this.objectMapper = objectMapper;
    }

    public List<PolicyRateItemSweden> scrapePolicyRateSweden() throws IOException {
        String endpointUrl = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";

        final var response = WebUtils.getHTTP(endpointUrl);
        final var scrapeType = new TypeReference<List<PolicyRateItemSweden>>() {};
        final var persistedPolicyRateItems = policyRateRepository.getPolicyRateSweden();
        final var novelScrapedItems = objectMapper.readValue(response, scrapeType)
                .stream()
                .filter(p -> !persistedPolicyRateItems.contains(p))
                .toList();
        policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
        log.info("Found %s new items from scraping, persisting do db...".formatted(novelScrapedItems.size()));

        return novelScrapedItems;
    }

}
