package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.services.ScrapeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    public Integer scrapePolicyRateItem(@PathVariable("country") String country) throws IOException {
        final var countryFormatted = country.toLowerCase().trim();
        log.info("Scraping policy rate for country: %s".formatted(countryFormatted));
        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);

        return switch (countryFormatted) {
            case "sweden" -> scrapeService.scrapePolicyRateSweden();
            case "eu" ->
                    throw new IllegalArgumentException("eu not implemented, feature is on it's way");
            case "usa" ->
                    throw new IllegalArgumentException("usa not implemented, feature is on it's way");
            default -> throw new IllegalArgumentException(e);
        };
    }

    @PostMapping("/exchange-rate/usd-sek")
    public Integer scrapeExchangeRateUsdSek() throws IOException {
        log.info("Scraping exchange rate for usd-sek");

        return scrapeService.scrapeExchangeRateUsdSek();
    }

    @PostMapping("government-bills/sweden")
    public Integer scrapeSwedishGovernmentBills(@RequestParam("period") String period) throws IOException {
        log.info("Scraping government bills for sweden");

        return scrapeService.scrapeGovernmentBillsSweden(period);
    }

    @PostMapping("government-bonds/sweden")
    public Integer scrapeSwedishGovernmentBonds(@RequestParam("period") String period) throws IOException {
        log.info("Scraping government bonds for sweden with period=%s".formatted(period));

        return scrapeService.scrapeGovernmentBondsSweden(period);
    }

    @PostMapping("euro-market-rate")
    public Integer scrapeEuroMarketRate(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) throws IOException {
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();
        log.info(("Scraping euro market rate with periodCountry=%s").formatted(periodCountry));

        return scrapeService.scrapeEuroMarketRate(periodCountry);
    }

}
