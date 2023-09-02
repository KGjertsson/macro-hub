package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.GovernmentBondItem;
import com.kg.macroanalyzer.repositories.GovernmentBondsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        final var e = ("Expected required query param 'period' to be one of ['2', '5', '7', '10'] " +
                "but found: %s").formatted(period);

        return switch (period) {
            case "2" -> governmentBondsRepository.getSwedishGovernmentBonds2Year();
            case "5" -> governmentBondsRepository.getSwedishGovernmentBonds5Year();
            case "7" -> governmentBondsRepository.getSwedishGovernmentBonds7Year();
            case "10" -> governmentBondsRepository.getSwedishGovernmentBonds10Year();
            default -> throw new IllegalArgumentException(e);
        };
    }

    @GetMapping("/international")
    public List<GovernmentBondItem> getInternationalGovernmentBondItems(
            @RequestParam("period") String period,
            @RequestParam("country") String country
    ) {
        final var msg = ("Received request for /government-bonds/international with " +
                "query parameters country=%s, period=%s").formatted(country, period);
        log.info(msg);
        final var e = "Found unexpected combination of query parameters country and period.";
        final var periodCountry = period.toLowerCase() + '-' + country.toLowerCase();

        return switch (periodCountry) {
            case "5year-eur" -> governmentBondsRepository.getIntGovBond5YearEur();
            case "5year-gb" -> governmentBondsRepository.getIntGovBond5YearGB();
            case "5year-japan" -> governmentBondsRepository.getIntGovBond5YearJapan();
            case "5year-usa" -> governmentBondsRepository.getIntGovBond5YearUsa();
            case "5year-france" -> governmentBondsRepository.getIntGovBond5YearFrance();
            case "5year-germany" -> governmentBondsRepository.getIntGovBond5YearGermany();
            case "5year-netherlands" -> governmentBondsRepository.getIntGovBond5YearHolland();
            case "10year-denmark" -> governmentBondsRepository.getIntGovBond10YearDenmark();
            case "10year-eur" -> governmentBondsRepository.getIntGovBond10YearEur();
            case "10year-finland" -> governmentBondsRepository.getIntGovBond10YearFinland();
            case "10year-france" -> governmentBondsRepository.getIntGovBond10YearFrance();
            case "10year-gb" -> governmentBondsRepository.getIntGovBond10YearGB();
            case "10year-germany" -> governmentBondsRepository.getIntGovBond10YearGermany();
            case "10year-japan" -> governmentBondsRepository.getIntGovBond10YearJapan();
            case "10year-netherlands" -> governmentBondsRepository.getIntGovBond10YearHolland();
            case "10year-norway" -> governmentBondsRepository.getIntGovBond10YearNorway();
            case "10year-usa" -> governmentBondsRepository.getIntGovBond10YearUsa();
            default -> throw new IllegalArgumentException(e);
        };
    }

}
