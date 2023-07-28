package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.PolicyRateItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PolicyRateController {

    @GetMapping("/policyRate/{country}")
    public List<PolicyRateItem> getPolicyRate(@PathVariable("country") String country) {
        return List.of();
    }

}
