package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.EuroMarketRateItem;
import com.kg.macroanalyzer.repositories.EuroMarketRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class EuroMarketRateController {

    private final EuroMarketRateRepository euroMarketRateRepository;

    @Autowired
    public EuroMarketRateController(EuroMarketRateRepository euroMarketRateRepository) {
        this.euroMarketRateRepository = euroMarketRateRepository;
    }

    @RequestMapping("/euro-market-rate")
    public List<EuroMarketRateItem> getEuroMarketRate(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) {
        final var msg = ("Received request for /euro-market-rate with query parameters " +
                "country=%s, period=%s").formatted(country, period);
        log.info(msg);
        final var e = "Found unexpected combination of query parameters country and period.";
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();

        return switch (periodCountry) {
            case "3month-denmark" -> euroMarketRateRepository.getEuroMarketRate3MonthDenmark();
            case "3month-eur" -> euroMarketRateRepository.getEuroMarketRate3MonthEur();
            case "3month-gb" -> euroMarketRateRepository.getEuroMarketRate3MonthGB();
            case "3month-japan" -> euroMarketRateRepository.getEuroMarketRate3MonthJapan();
            case "3month-norway" -> euroMarketRateRepository.getEuroMarketRate3MonthNorway();
            case "3month-usa" -> euroMarketRateRepository.getEuroMarketRate3MonthUsa();
            case "6month-denmark" -> euroMarketRateRepository.getEuroMarketRate6MonthDenmark();
            case "6month-eur" -> euroMarketRateRepository.getEuroMarketRate6MonthEur();
            case "6month-gb" -> euroMarketRateRepository.getEuroMarketRate6MonthGB();
            case "6month-japan" -> euroMarketRateRepository.getEuroMarketRate6MonthJapan();
            case "6month-norway" -> euroMarketRateRepository.getEuroMarketRate6MonthNorway();
            case "6month-usa" -> euroMarketRateRepository.getEuroMarketRate6MonthUsa();
            default -> throw new IllegalArgumentException(e);
        };
    }

}
