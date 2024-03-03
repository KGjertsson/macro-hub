package com.kg.macroanalyzer.adaptors.database.postgres.models;

import lombok.Builder;
import lombok.NonNull;
import org.jooq.Record;

import java.time.Instant;
import java.time.ZoneOffset;

import static com.kg.macroanalyzer.jooq.generated.Tables.SCRAPE_ACTION_QUEUE;

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

    public static ScrapeQueueItem of(Record r) {
        return ScrapeQueueItem.builder()
                .id(r.getValue(SCRAPE_ACTION_QUEUE.ID))
                .name(r.getValue(SCRAPE_ACTION_QUEUE.DATASET_NAME))
                .scrapeDate(r.getValue(SCRAPE_ACTION_QUEUE.SCRAPE_DATE).toInstant(ZoneOffset.UTC))
                .build();
    }

}
