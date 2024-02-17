package com.kg.macroanalyzer.adaptors.http.controllers;

import com.kg.macroanalyzer.adaptors.http.services.*;
import com.kg.macroanalyzer.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

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
    public ResponseEntity<Void> scrapePolicyRateItem(@PathVariable("country") String country) {
        return Stream.ofNullable(country)
                .map(String::toLowerCase)
                .map(String::trim)
                .peek(c -> log.info("Scraping policy rate for country: %s".formatted(c)))
                .map(this::enqueuePolicyRateSweden)
                .map(this::toResponseEntity)
                .toList()
                .getFirst();
    }

    @PostMapping("/exchange-rate/usd-sek")
    public ResponseEntity<Void> scrapeExchangeRateUsdSek() {
        log.info("Scraping exchange rate for usd-sek");

        return Stream.ofNullable(exchangeRateScrapeService.scrapeExchangeRateUsdSek())
                .map(this::toResponseEntity)
                .toList()
                .getFirst();
    }

    @PostMapping("government-bills/sweden")
    public ResponseEntity<Void> scrapeSwedishGovBills(@RequestParam("period") String period) {
        return Stream.ofNullable(period)
                .peek(p -> log.info("Scraping government bills for sweden"))
                .map(governmentBillScrapeService::scrapeGovernmentBillsSweden)
                .map(this::toResponseEntity)
                .toList()
                .getFirst();
    }

    @PostMapping("government-bonds/sweden")
    public ResponseEntity<Void> scrapeSwedishGovBonds(@RequestParam("period") String period) {
        return Stream.ofNullable(period)
                .map("%syear-swe"::formatted)
                .peek(pc -> log.info("Scraping swedish gov bonds with period=%s".formatted(pc)))
                .map(governmentBondScrapeService::scrapeGovBonds)
                .map(this::toResponseEntity)
                .toList()
                .getFirst();
    }

    @PostMapping("government-bonds/international")
    public ResponseEntity<Void> scrapeIntGovBonds(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) {
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();

        return Stream.ofNullable(periodCountry)
                .peek(pc -> log.info(("Scraping int gov bonds with pc=%s").formatted(pc)))
                .map(governmentBondScrapeService::scrapeGovBonds)
                .map(this::toResponseEntity)
                .toList()
                .getFirst();
    }

    @PostMapping("euro-market-rate")
    public ResponseEntity<Void> scrapeEuroMarketRate(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) {
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();

        return Stream.ofNullable(periodCountry)
                .peek(pc -> log.info(("Scraping euro market rate with pc=%s").formatted(pc)))
                .map(euroMarketRateScrapeService::scrapeEuroMarketRate)
                .map(this::toResponseEntity)
                .toList()
                .getFirst();
    }

    @PostMapping("schedule-all")
    public Integer scheduleAll(
            @RequestParam("interval") String interval,
            @RequestParam(name = "multiplier", defaultValue = "1") Integer multiplier
    ) {
        final var cronInterval = stringToChronoUnit(interval);

        return scrapeService.scheduleAll(cronInterval, multiplier);
    }

    private Integer enqueuePolicyRateSweden(String countryFormatted) {
        final var msgRaw = "Unexpected country value, expected one of ['sweden'], but found: %s";
        final var msgFormatted = msgRaw.formatted(countryFormatted);

        return switch (countryFormatted) {
            case "sweden" -> policyRateScrapeService.scrapePolicyRateSweden();
            case "eu" -> throw new IllegalArgumentException("eu not implemented");
            case "usa" -> throw new IllegalArgumentException("usa not implemented");
            default -> throw new IllegalArgumentException(msgFormatted);
        };
    }

    private ResponseEntity<Void> toResponseEntity(Integer result) {
        if (result != null && result == 1) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
