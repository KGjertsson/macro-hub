package com.kg.macroanalyzer.scrape.domain;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

@Builder(toBuilder = true)
public record ScrapeQueueItem(
        Integer id,
        @NonNull String name,
        @NonNull Instant scrapeDate
) {

    public static ScrapeQueueItem of(String name) {
        return ScrapeQueueItem.of(name, Instant.now());
    }

    public static ScrapeQueueItem of(String name, Instant scrapeDate) {
        return ScrapeQueueItem.builder()
                .name(name)
                .scrapeDate(scrapeDate)
                .build();
    }

    public static ScrapeQueueItem of(Integer id, String name, Instant scrapeDate) {
        return ScrapeQueueItem.of(name, scrapeDate).toBuilder()
                .id(id)
                .build();
    }

}
