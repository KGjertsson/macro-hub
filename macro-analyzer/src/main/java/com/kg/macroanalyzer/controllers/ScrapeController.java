package com.kg.macroanalyzer.controllers;

import com.kg.macroanalyzer.models.PolicyRateItem;
import com.kg.macroanalyzer.models.PolicyRateSweden;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
    public List<PolicyRateItem> scrapePolicyRateItem(@PathVariable("country") String country) {
        final var countryFormatted = country.toLowerCase().trim();
        final var e = "Unexpected country value, expected one of ['sweden'], but found: %s".formatted(countryFormatted);

        switch (countryFormatted) {
            case "sweden":
                return scrapePolicyRateSweden(countryFormatted);
            default:
                throw new IllegalArgumentException(e);
        }

    }

    private List<PolicyRateItem> scrapePolicyRateSweden(String countryFormatted) {
            final var persistedPolicyRateItems = policyRateRepository.getPolicyRateSweden();

            String endpointUrl = "https://api-test.riksbank.se/swea/v1/Series/SECBREPOEFF?en";

            try {
                URL url = new URL(endpointUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                // Get the response as a string
                String response = responseBuilder.toString();
                System.out.println("Response: " + response);

                // Close the connection
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return List.of();
    }

}
