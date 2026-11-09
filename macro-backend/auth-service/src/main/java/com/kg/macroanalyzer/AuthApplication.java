package com.kg.macroanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Not the whole root package, unlike the other services: auth has no scraping, so macro-platform's scrape beans, crons
// and db/platform migrations stay out and only shared is picked up
@SpringBootApplication(scanBasePackages = {"com.kg.macroanalyzer.auth", "com.kg.macroanalyzer.shared"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
