package com.kg.macroanalyzer.models;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

@Builder
public record ScrapeQueueItem(@NonNull String name, @NonNull Instant scrapeDate) {

    public static ScrapeQueueItem of(String name, Instant scrapeDate) {
        return ScrapeQueueItem.builder()
                .name(name)
                .scrapeDate(scrapeDate)
                .build();
    }

}
