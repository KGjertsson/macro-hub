package com.kg.macroanalyzer.services;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@Slf4j
@Service
public class ScrapeService {

    private final ScrapeRepository scrapeRepository;

    @Value("${scrape.data.names}")
    private String scrapeDataNames;

    @Autowired
    public ScrapeService(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    public Integer scheduleAll(ChronoUnit interval, Integer multiplier) {
        return Arrays.stream(scrapeDataNames.split(","))
                .reduce(initializer(), accumulator(interval, multiplier), combiner())
                .stream()
                .map(scrapeRepository::addScrapeQueueItem)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private ArrayList<ScrapeQueueItem> initializer() {
        return new ArrayList<>();
    }

    private BiFunction<ArrayList<ScrapeQueueItem>, String, ArrayList<ScrapeQueueItem>> accumulator(
            ChronoUnit interval,
            Integer multiplier
    ) {
        return (acc, element) -> {
            if (acc.isEmpty()) {
                final var scrapeQueueItem = ScrapeQueueItem.of(element, Instant.now());
                final var initialized = new ArrayList<ScrapeQueueItem>();
                initialized.add(scrapeQueueItem);

                return initialized;
            } else {
                final var last = acc.get(acc.size() - 1).scrapeDate();
                final var next = last.plus(multiplier, interval);
                final var scrapeQueueItem = ScrapeQueueItem.of(element, next);
                acc.add(scrapeQueueItem);

                return acc;
            }
        };
    }

    private BinaryOperator<ArrayList<ScrapeQueueItem>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }

}
