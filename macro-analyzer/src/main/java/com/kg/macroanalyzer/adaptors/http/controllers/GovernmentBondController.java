package com.kg.macroanalyzer.adaptors.http.controllers;

import com.kg.macroanalyzer.adaptors.database.postgres.models.GovernmentBondItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.GovernmentBondsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("government-bonds")
public class GovernmentBondController {

    private final GovernmentBondsRepository governmentBondsRepository;

    @Autowired
    public GovernmentBondController(GovernmentBondsRepository governmentBondsRepository) {
        this.governmentBondsRepository = governmentBondsRepository;
    }

    @GetMapping("/sweden")
    public List<GovernmentBondItem> getSwedishGovernmentBondItems(@RequestParam("period") String period) {
        log.info("Received request for /government-bonds/sweden with period = %s".formatted(period));

        return Stream.ofNullable(period)
                .map(this::getGovBondReader)
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Supplier<List<GovernmentBondItem>> getGovBondReader(String period) {
        final var errorRaw = "Expected param 'period' to be one of [2, 5, 7, 10] but found: %s";
        final var errorFormatted = errorRaw.formatted(period);

        return switch (period) {
            case "2" -> governmentBondsRepository.swedishGovBond2YearReader();
            case "5" -> governmentBondsRepository.swedishGovBond5YearReader();
            case "7" -> governmentBondsRepository.swedishGovBond7YearReader();
            case "10" -> governmentBondsRepository.swedishGovBond10YearReader();
            default -> throw new IllegalArgumentException(errorFormatted);
        };
    }

    @GetMapping("/international")
    public List<GovernmentBondItem> getInternationalGovernmentBondItems(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) {
        final var msgRaw = "GET int gov bonds with query parameters country=%s, period=%s";
        final var msgFormatted = msgRaw.formatted(country, period);
        log.info(msgFormatted);
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();

        return Stream.ofNullable(periodCountry)
                .map(this::getIntGovBondReader)
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Supplier<List<GovernmentBondItem>> getIntGovBondReader(String periodCountry) {
        final var e = "Found unexpected combination of query parameters country and period.";

        return switch (periodCountry) {
            case "5year-eur" -> governmentBondsRepository.intGovBond5YearReaderEur();
            case "5year-gb" -> governmentBondsRepository.intGovBond5YearReaderGB();
            case "5year-japan" -> governmentBondsRepository.intGovBond5YearReaderJapan();
            case "5year-usa" -> governmentBondsRepository.intGovBond5YearReaderUSA();
            case "5year-france" -> governmentBondsRepository.intGovBond5YearReaderFrance();
            case "5year-germany" -> governmentBondsRepository.intGovBond5YearReaderGermany();
            case "5year-netherlands" -> governmentBondsRepository.intGovBond5YearReaderHolland();
            case "10year-denmark" -> governmentBondsRepository.intGovBond10YearReaderDenmark();
            case "10year-eur" -> governmentBondsRepository.intGovBond10YearReaderEur();
            case "10year-finland" -> governmentBondsRepository.intGovBond10YearReaderFinland();
            case "10year-france" -> governmentBondsRepository.intGovBond10YearReaderFrance();
            case "10year-gb" -> governmentBondsRepository.intGovBond10YearReaderGB();
            case "10year-germany" -> governmentBondsRepository.intGovBond10YearReaderGermany();
            case "10year-japan" -> governmentBondsRepository.intGovBond10YearReaderJapan();
            case "10year-netherlands" -> governmentBondsRepository.intGovBond10YearReaderHolland();
            case "10year-norway" -> governmentBondsRepository.intGovBond10YearReaderNorway();
            case "10year-usa" -> governmentBondsRepository.intGovBond10YearReaderUSA();
            default -> throw new IllegalArgumentException(e);
        };
    }

}
