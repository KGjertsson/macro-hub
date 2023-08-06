package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("policy-rate")
public class PolicyRateController {

    private final PolicyRateRepository policyRateRepository;

    @Autowired
    public PolicyRateController(PolicyRateRepository policyRateRepository) {
        this.policyRateRepository = policyRateRepository;
    }

    @GetMapping("/{country}")
    public List<PolicyRateItem> getPolicyRate(@PathVariable("country") String country) {
        final var countryFormatted = country.toLowerCase().trim();
        log.info("Received request for /policy-rate/%s".formatted(countryFormatted));

        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);
        return switch (countryFormatted) {
            case "sweden" -> policyRateRepository.getPolicyRateSweden();
            case "usa" -> throw new IllegalArgumentException("usa not implemented, feature is on it's way");
            case "eu" -> throw new IllegalArgumentException("eu not implemented, feature is on it's way");
            default -> throw new IllegalArgumentException(e);
        };

    }

}
