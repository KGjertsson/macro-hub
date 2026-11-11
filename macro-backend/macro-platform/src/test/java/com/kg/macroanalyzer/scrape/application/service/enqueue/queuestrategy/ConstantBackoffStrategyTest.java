package com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantBackoffStrategyTest {

    @Test
    void shouldSpaceItemsFiveMinutesApart_byQueuePosition() {
        // given
        final var strategy = new ConstantBackoffStrategy();
        final var now = Instant.parse("2026-11-05T22:00:00Z");

        // when
        final var dates = IntStream.range(0, 3)
                .mapToObj(position -> strategy.scrapeDate(now, position))
                .toList();

        // then
        assertEquals(
                List.of(now, now.plus(Duration.ofMinutes(5)), now.plus(Duration.ofMinutes(10))),
                dates);
    }

    @Test
    void shouldGiveTheSameDate_whenAskedTwiceForTheSamePosition() {
        // given
        final var strategy = new ConstantBackoffStrategy();
        final var now = Instant.parse("2026-11-05T22:00:00Z");

        // when
        final var first = strategy.scrapeDate(now, 2);
        final var second = strategy.scrapeDate(now, 2);

        // then
        assertEquals(first, second);
    }

}
