package com.kg.macroanalyzer.adaptors.http.controllers;

import com.kg.macroanalyzer.adaptors.database.postgres.repositories.EuroMarketRateRepository;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
public class EuroMarketRateController {

    private final EuroMarketRateRepository euroMarketRateRepository;

    @Autowired
    public EuroMarketRateController(EuroMarketRateRepository euroMarketRateRepository) {
        this.euroMarketRateRepository = euroMarketRateRepository;
    }

    @RequestMapping("/euro-market-rate")
    public List<MacroPoint> getEuroMarketRate(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) {
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();
        log.info("GET /euro-market-rate with query period-country=%s".formatted(periodCountry));

        return Stream.ofNullable(periodCountry)
                .map(this::getEuroMarketRateReader)
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public Supplier<List<MacroPoint>> getEuroMarketRateReader(String periodCountry) {
        final var e = "Found unexpected combination of query parameters country and period.";

        return switch (periodCountry) {
            case "3month-denmark" -> euroMarketRateRepository.euroMarketRate3MonthDenmark();
            case "3month-eur" -> euroMarketRateRepository.euroMarketRate3MonthEur();
            case "3month-gb" -> euroMarketRateRepository.euroMarketRate3MonthGB();
            case "3month-japan" -> euroMarketRateRepository.euroMarketRate3MonthJapan();
            case "3month-norway" -> euroMarketRateRepository.euroMarketRate3MonthNorway();
            case "3month-usa" -> euroMarketRateRepository.euroMarketRate3MonthUsa();
            case "6month-denmark" -> euroMarketRateRepository.euroMarketRate6MonthDenmark();
            case "6month-eur" -> euroMarketRateRepository.getEuroMarketRate6MonthEur();
            case "6month-gb" -> euroMarketRateRepository.getEuroMarketRate6MonthGB();
            case "6month-japan" -> euroMarketRateRepository.getEuroMarketRate6MonthJapan();
            case "6month-norway" -> euroMarketRateRepository.getEuroMarketRate6MonthNorway();
            case "6month-usa" -> euroMarketRateRepository.getEuroMarketRate6MonthUsa();
            default -> throw new IllegalArgumentException(e);
        };
    }

}
