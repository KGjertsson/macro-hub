package com.kg.macroanalyzer.scrape.domain;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

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

    public static ScrapeQueueItem of(Integer id, String name, Instant scrapeDate) {
        return ScrapeQueueItem.of(name, scrapeDate).toBuilder()
                .id(id)
                .build();
    }

    private static ScrapeQueueKind resolveKind(String name) {
        return switch (name) {
            case "MembersOfParliament" -> ScrapeQueueKind.MEMBER_OF_PARLIAMENT;
            case "MembersOfParliamentActivity" -> ScrapeQueueKind.MEMBER_ACTIVITY;
            case "MunicipalElections", "RegionElections", "ParliamentElections" -> ScrapeQueueKind.LOCAL_ELECTION;
            case "ScbRegionalStatistics" -> ScrapeQueueKind.SCB_REGIONAL;
            default -> ScrapeQueueKind.MACRO_POINT;
        };
    }

}
