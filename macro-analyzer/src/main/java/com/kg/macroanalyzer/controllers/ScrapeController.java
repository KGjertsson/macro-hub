package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.exchangerate.ExchangeRateUsdSek;
import com.kg.macroanalyzer.models.governmentbills.GovernmentBillItem;
import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import com.kg.macroanalyzer.services.ScrapeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scrape/")
public class ScrapeController {

    private final ScrapeService scrapeService;

    @Autowired
    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @PostMapping("/policy-rate/{country}")
    public List<PolicyRateItem> scrapePolicyRateItem(@PathVariable("country") String country) throws IOException {
        final var countryFormatted = country.toLowerCase().trim();
        log.info("Scraping policy rate for country: %s".formatted(countryFormatted));
        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);

        return switch (countryFormatted) {
            case "sweden" -> scrapeService.scrapePolicyRateSweden();
            case "eu" -> throw new IllegalArgumentException("eu not implemented, feature is on it's way");
            case "usa" -> throw new IllegalArgumentException("usa not implemented, feature is on it's way");
            default -> throw new IllegalArgumentException(e);
        };

    }

    @PostMapping("/exchange-rate/usd-sek")
    public List<ExchangeRateUsdSek> scrapeExchangeRateUsdSek() throws IOException {
        log.info("Scraping exchange rate for usd-sek");

        return scrapeService.scrapeExchangeRateUsdSek();
    }

    @PostMapping("government-bills/sweden")
    public List<GovernmentBillItem> scrapeSwedishGovernmentBills() throws IOException {
        log.info("Scraping government bills for sweden");

        return scrapeService.scrapeGovernmentBillsSweden();
    }

}
