package com.kg.macroanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// In the root package, so component scanning also finds macro-platform's beans (com.kg.macroanalyzer.scrape and .shared)
@SpringBootApplication
public class ParliamentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParliamentApplication.class, args);
    }

}
