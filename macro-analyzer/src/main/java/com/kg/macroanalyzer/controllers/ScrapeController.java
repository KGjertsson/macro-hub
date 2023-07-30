package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.PolicyRateItem;
import com.kg.macroanalyzer.models.PolicyRateSweden;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scrape/")
public class ScrapeController {

    private final PolicyRateRepository policyRateRepository;

    @Autowired
    public ScrapeController(PolicyRateRepository policyRateRepository) {
        this.policyRateRepository = policyRateRepository;
    }

    @PostMapping("/policy-rate/{country}")
    public PolicyRateSweden scrapePolicyRateItem(@PathVariable("country") String country) {
        final var countryFormatted = country.toLowerCase().trim();
        final var persistedPolicyRateItems = getPersistedPolicyRateItems(countryFormatted);

        return new PolicyRateSweden();
    }

    private List<PolicyRateItem> getPersistedPolicyRateItems(String countryFormatted) {
        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);

        switch (countryFormatted) {
            case "sweden":
                return policyRateRepository.getPolicyRateSweden();
            default:
                throw new IllegalArgumentException(e);
        }
    }

}
