package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.temporal.ChronoUnit;

@Slf4j
@RestController
@RequestMapping("/scrape/")
public class ScrapeController {

    private final PolicyRateScrapeService policyRateScrapeService;
    private final ExchangeRateScrapeService exchangeRateScrapeService;
    private final GovernmentBillScrapeService governmentBillScrapeService;
    private final GovernmentBondScrapeService governmentBondScrapeService;
    private final EuroMarketRateScrapeService euroMarketRateScrapeService;
    private final ScrapeService scrapeService;

    @Autowired
    public ScrapeController(
            PolicyRateScrapeService policyRateScrapeService,
            ExchangeRateScrapeService exchangeRateScrapeService,
            GovernmentBillScrapeService governmentBillScrapeService,
            GovernmentBondScrapeService governmentBondScrapeService,
            EuroMarketRateScrapeService euroMarketRateScrapeService,
            ScrapeService scrapeService
    ) {
        this.policyRateScrapeService = policyRateScrapeService;
        this.exchangeRateScrapeService = exchangeRateScrapeService;
        this.governmentBillScrapeService = governmentBillScrapeService;
        this.governmentBondScrapeService = governmentBondScrapeService;
        this.euroMarketRateScrapeService = euroMarketRateScrapeService;
        this.scrapeService = scrapeService;
    }

    @PostMapping("/policy-rate/{country}")
    public Integer scrapePolicyRateItem(
            @PathVariable("country") String country
    ) throws IOException {
        final var countryFormatted = country.toLowerCase().trim();
        log.info("Scraping policy rate for country: %s".formatted(countryFormatted));
        final var e = ("Unexpected country value, expected one of ['sweden'], " +
                "but found: %s").formatted(countryFormatted);

        return switch (countryFormatted) {
            case "sweden" -> policyRateScrapeService.scrapePolicyRateSweden();
            case "eu" -> throw new IllegalArgumentException("eu not implemented");
            case "usa" -> throw new IllegalArgumentException("usa not implemented");
            default -> throw new IllegalArgumentException(e);
        };
    }

    @PostMapping("/exchange-rate/usd-sek")
    public Integer scrapeExchangeRateUsdSek() throws IOException {
        log.info("Scraping exchange rate for usd-sek");

        return exchangeRateScrapeService.scrapeExchangeRateUsdSek();
    }

    @PostMapping("government-bills/sweden")
    public Integer scrapeSwedishGovernmentBills(
            @RequestParam("period") String period
    ) {
        log.info("Scraping government bills for sweden");

        return governmentBillScrapeService.scrapeGovernmentBillsSweden(period);
    }

    @PostMapping("government-bonds/sweden")
    public Integer scrapeSwedishGovernmentBonds(
            @RequestParam("period") String period
    ) throws IOException {
        log.info("Scraping government bonds for sweden with period=%s".formatted(period));

        return governmentBondScrapeService.scrapeGovernmentBondsSweden(period);
    }

    @PostMapping("government-bonds/international")
    public Integer scrapeIntGovBonds(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) throws IOException {
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();
        log.info(("Scraping international government bonds with periodCountry=%s").formatted(periodCountry));

        return governmentBondScrapeService.scrapeIntGovBonds(periodCountry);
    }

    @PostMapping("euro-market-rate")
    public Integer scrapeEuroMarketRate(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) throws IOException {
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();
        log.info(("Scraping euro market rate with periodCountry=%s").formatted(periodCountry));

        return euroMarketRateScrapeService.scrapeEuroMarketRate(periodCountry);
    }

    @PostMapping("schedule-all")
    public Integer scheduleAll(
            @RequestParam("interval") String interval,
            @RequestParam(name = "multiplier", defaultValue = "1") Integer multiplier
    ) {
        final var chronoInterval = stringToChronoUnit(interval);

        return scrapeService.scheduleAll(chronoInterval, multiplier);
    }

    private ChronoUnit stringToChronoUnit(String interval) {
        final var errorMsg = ("Expected argument interval to be one of [minute, hour, day] "
                + "but found %s").formatted(interval);

        return switch (interval.toLowerCase()) {
            case "minute" -> ChronoUnit.MINUTES;
            case "hour" -> ChronoUnit.HOURS;
            case "day" -> ChronoUnit.DAYS;
            default -> throw new IllegalArgumentException(errorMsg);
        };
    }

}
