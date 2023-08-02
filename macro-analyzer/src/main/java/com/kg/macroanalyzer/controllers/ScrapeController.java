package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.exchangerate.ExchangeRateItem;
import com.kg.macroanalyzer.models.policyrate.PolicyRateItemSweden;
import com.kg.macroanalyzer.services.ScrapeService;
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

    private final ScrapeService scrapeService;

    @Autowired
    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @PostMapping("/policy-rate/{country}")
    public List<PolicyRateItemSweden> scrapePolicyRateItem(@PathVariable("country") String country) throws IOException {
        final var countryFormatted = country.toLowerCase().trim();
        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);

        return switch (countryFormatted) {
            case "sweden" -> scrapeService.scrapePolicyRateSweden();
            case "eu" -> throw new IllegalArgumentException("eu not implemented, feature is on it's way");
            case "usa" -> throw new IllegalArgumentException("usa not implemented, feature is on it's way");
            default -> throw new IllegalArgumentException(e);
        };

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
