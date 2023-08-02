package com.kg.macroanalyzer.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.models.exchangerate.ExchangeRateItem;
import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/scrape/")
public class ScrapeController {

    private final PolicyRateRepository policyRateRepository;

    @Autowired
    public ScrapeController(PolicyRateRepository policyRateRepository) {
        this.policyRateRepository = policyRateRepository;
    }

    @PostMapping("/policy-rate/{country}")
    public List<PolicyRateItem> scrapePolicyRateItem(@PathVariable("country") String country) {
        final var countryFormatted = country.toLowerCase().trim();
        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);

        switch (countryFormatted) {
            case "sweden":
                return scrapePolicyRateSweden(countryFormatted);
            default:
                throw new IllegalArgumentException(e);
        }

    }

    private List<PolicyRateItem> scrapePolicyRateSweden(String countryFormatted) {

//            String endpointUrl = "https://api-test.riksbank.se/swea/v1/Series/SECBREPOEFF?en";

        String endpointUrl = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";

        try {
            final var response = WebUtils.getHTTP(endpointUrl);
            final var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            final var scrapedPolicyRateItems = objectMapper.readValue(response, new TypeReference<List<PolicyRateItem>>(){});
            final var persistedPolicyRateItems = policyRateRepository.getPolicyRateSweden();

            final var foo = 1;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return List.of();
    }

    @PostMapping("/exchange-rate/usd-sek")
    public List<ExchangeRateItem> scrapeExchangeRateUsdSek() {
        try {
            final var usd = "SEKUSDPMI";
            final var CNY = "SEKCNYPMI";
            final var from = "1994-03-01";
            final var endpointUrl = "https://api-test.riksbank.se/swea/v1/CrossRates/%s/%s/%s".formatted(usd, CNY, from);
            final var response = WebUtils.getHTTP(endpointUrl);
            return List.of();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

}
