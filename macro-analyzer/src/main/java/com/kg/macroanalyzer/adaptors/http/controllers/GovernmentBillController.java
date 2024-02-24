package com.kg.macroanalyzer.adaptors.http.controllers;

import com.kg.macroanalyzer.adaptors.database.postgres.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.application.domain.MacroPoint;
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
@RequestMapping("government-bills")
public class GovernmentBillController {

    private final GovernmentBillRepository governmentBillRepository;

    @Autowired
    public GovernmentBillController(GovernmentBillRepository governmentBillRepository) {
        this.governmentBillRepository = governmentBillRepository;
    }

    @GetMapping("/sweden")
    public List<MacroPoint> getSwedishGovernmentBillItems(@RequestParam("period") String period) {
        log.info("GET request for /government-bills/sweden with period = %s".formatted(period));

        return Stream.ofNullable(period)
                .map(this::getGovBillReader)
                .map(Supplier::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Supplier<List<MacroPoint>> getGovBillReader(String period) {
        final var errorRaw = "Expected param 'period' to be one of [1, 3, 6, 12] but found: %s";
        final var errorFormatted = errorRaw.formatted(period);

        return switch (period) {
            case "1" -> governmentBillRepository.swedishGovBills1MonthReader();
            case "3" -> governmentBillRepository.swedishGovBills3MonthReader();
            case "6" -> governmentBillRepository.swedishGovBills6MonthReader();
            case "12" -> governmentBillRepository.swedishGovBills12MonthReader();
            default -> throw new IllegalArgumentException(errorFormatted);
        };
    }

}
