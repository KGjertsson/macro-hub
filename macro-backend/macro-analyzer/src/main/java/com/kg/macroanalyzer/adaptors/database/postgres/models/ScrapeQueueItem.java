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
        @NonNull Instant scrapeDate,
        @NonNull ScrapeQueueKind scrapeQueueKind
) {

    public static ScrapeQueueItem of(String name) {
        return ScrapeQueueItem.of(name, Instant.now());
    }

    public static ScrapeQueueItem of(String name, Instant scrapeDate) {
        final var scrapeQueueKind = resolveKind(name);

        return ScrapeQueueItem.builder()
                .name(name)
                .scrapeDate(scrapeDate)
                .scrapeQueueKind(scrapeQueueKind)
                .build();
    }

    public static ScrapeQueueItem of(Record r) {
        final var name = r.getValue(SCRAPE_ACTION_QUEUE.DATASET_NAME);
        final var scrapeQueueKind = resolveKind(name);

        return ScrapeQueueItem.builder()
                .id(r.getValue(SCRAPE_ACTION_QUEUE.ID))
                .name(name)
                .scrapeDate(r.getValue(SCRAPE_ACTION_QUEUE.SCRAPE_DATE).toInstant(ZoneOffset.UTC))
                .scrapeQueueKind(scrapeQueueKind)
                .build();
    }

    private static ScrapeQueueKind resolveKind(String name) {
        return name.equals("MembersOfParliament")
                ? ScrapeQueueKind.MEMBER_OF_PARLIAMENT
                : ScrapeQueueKind.MACRO_POINT;
    }

}
