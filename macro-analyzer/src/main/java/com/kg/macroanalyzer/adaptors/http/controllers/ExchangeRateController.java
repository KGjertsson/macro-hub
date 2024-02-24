package com.kg.macroanalyzer.adaptors.http.controllers;

import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ExchangeRateRepository;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("exchange-rate")
public class ExchangeRateController {

    private final ExchangeRateRepository exchangeRateRepository;

    @Autowired
    public ExchangeRateController(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @GetMapping("/usd-sek")
    public List<MacroPoint> getExchangeRateUsdSek() {
        log.info("Received request for /exchange-rate/usd-sek");

        return exchangeRateRepository.getExchangeRateUsdSek().get();
    }

}
