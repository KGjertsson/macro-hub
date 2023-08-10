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
            case "5year-denmark" -> euroMarketRateRepository.getEuroMarketRate5YearDenmark();
            case "5year-eur" -> euroMarketRateRepository.getEuroMarketRate5YearEur();
            case "5year-gb" -> euroMarketRateRepository.getEuroMarketRate5YearGB();
            case "5year-japan" -> euroMarketRateRepository.getEuroMarketRate5YearJapan();
            case "5year-norway" -> euroMarketRateRepository.getEuroMarketRate5YearNorway();
            case "5year-usa" -> euroMarketRateRepository.getEuroMarketRate5YearUsa();
            case "10year-denmark" -> euroMarketRateRepository.getEuroMarketRate10YearDenmark();
            case "10year-eur" -> euroMarketRateRepository.getEuroMarketRate10YearEur();
            case "10year-finland" -> euroMarketRateRepository.getEuroMarketRate10YearFinland();
            case "10year-france" -> euroMarketRateRepository.getEuroMarketRate10YearFrance();
            case "10year-gb" -> euroMarketRateRepository.getEuroMarketRate10YearGB();
            case "10year-germany" -> euroMarketRateRepository.getEuroMarketRate10YearGermany();
            case "10year-japan" -> euroMarketRateRepository.getEuroMarketRate10YearJapan();
            case "10year-netherlands" -> euroMarketRateRepository.getEuroMarketRate10YearHolland();
            case "10year-norway" -> euroMarketRateRepository.getEuroMarketRate10YearNorway();
            case "10year-usa" -> euroMarketRateRepository.getEuroMarketRate10YearUsa();
            default -> throw new IllegalArgumentException(e);
        };
    }

}
