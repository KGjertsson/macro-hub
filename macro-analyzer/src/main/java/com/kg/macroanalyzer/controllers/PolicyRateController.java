package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);

        switch (countryFormatted) {
            case "sweden":
                return policyRateRepository.getPolicyRateSweden();
            default:
                throw new IllegalArgumentException(e);
        }

    }

}
