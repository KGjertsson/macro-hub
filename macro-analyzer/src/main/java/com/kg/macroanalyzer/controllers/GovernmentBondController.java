package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.governmentbonds.GovernmentBondItem;
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
            case "2" -> governmentBondsRepository.getSwedishGovernmentBonds2Month();
            case "5" -> governmentBondsRepository.getSwedishGovernmentBonds5Month();
            case "7" -> governmentBondsRepository.getSwedishGovernmentBonds7Month();
            case "10" -> governmentBondsRepository.getSwedishGovernmentBonds10Month();
            default -> throw new IllegalArgumentException(e);
        };

    }

}
