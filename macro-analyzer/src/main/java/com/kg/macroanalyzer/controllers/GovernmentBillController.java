package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.governmentbills.GovernmentBillItem;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("government-bills")
public class GovernmentBillController {

    private final GovernmentBillRepository governmentBillRepository;

    @Autowired
    public GovernmentBillController(GovernmentBillRepository governmentBillRepository) {
        this.governmentBillRepository = governmentBillRepository;
    }

    @GetMapping("/sweden")
    public List<GovernmentBillItem> getSwedishGovernmentBillItems(@RequestParam("period") String period) {
        log.info("Received request for /government-bills/sweden with period = %s".formatted(period));
        final var e = ("Expected required query param 'period' to be one of ['1', '3', '6', '12'] " +
                "but found: %s").formatted(period);

        return switch (period) {
            case "1" -> governmentBillRepository.getSwedishGovernmentBills1Month();
            case "3" -> governmentBillRepository.getSwedishGovernmentBills3Month();
            case "6" -> governmentBillRepository.getSwedishGovernmentBills6Month();
            case "12" -> governmentBillRepository.getSwedishGovernmentBills12Month();
            default -> throw new IllegalArgumentException(e);
        };

    }

}
