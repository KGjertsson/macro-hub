package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicatorResult;
import com.kg.macroanalyzer.application.domain.scb.ScbSource;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithScbIndicators;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScbRegionalPersisterTest {

    private static final Instant UPDATED = Instant.parse("2026-03-24T07:00:00Z");

    @Mock
    DatabasePort databasePort;

    @Test
    void shouldWriteEveryIndicator_andMarkAsDone() {
        // given
        final var config = SeriesConfig.builder().name("ScbRegionalStatistics").build();
        final var population = result("population", "TAB6574");
        final var households = result("households", "TAB6568");

        // when
        when(databasePort.writeScbIndicatorResult(any())).thenReturn(1);
        final var scrapeResult = new ScbRegionalPersister(databasePort).persist(input(config, List.of(population, households)));

        // then
        assertEquals(ScrapeResult.SUCCESS, scrapeResult);
        verify(databasePort).markAsDone(config);
        verify(databasePort).writeScbIndicatorResult(population);
        verify(databasePort).writeScbIndicatorResult(households);
        verify(databasePort).writeScbTableStamps(Map.of("TAB6574", UPDATED, "TAB6568", UPDATED), Set.of());
    }

    @Test
    void shouldWriteStampsAfterEveryIndicator_andDropTheTablesOfAFailedWrite() {
        // given
        final var config = SeriesConfig.builder().name("ScbRegionalStatistics").build();
        final var population = result("population", "TAB6574");
        final var share65 = result("share_65_plus", "TAB6574");
        final var households = result("households", "TAB6568");
        final var order = inOrder(databasePort);

        // when
        when(databasePort.writeScbIndicatorResult(population)).thenReturn(1);
        when(databasePort.writeScbIndicatorResult(share65)).thenThrow(new RuntimeException("connection reset"));
        when(databasePort.writeScbIndicatorResult(households)).thenReturn(1);
        final var scrapeResult = new ScbRegionalPersister(databasePort)
                .persist(input(config, List.of(population, share65, households), Set.of("TAB6589")));

        // then
        assertEquals(ScrapeResult.SUCCESS, scrapeResult);
        order.verify(databasePort).writeScbIndicatorResult(households);
        // TAB6574 is shared with the failed share_65_plus, so population's new stamp for it is not stored either
        order.verify(databasePort).writeScbTableStamps(Map.of("TAB6568", UPDATED), Set.of("TAB6574", "TAB6589"));
        order.verify(databasePort).markAsDone(config);
    }

    @Test
    void shouldFail_andLeaveTheItemQueued_whenEveryWriteFails() {
        // given
        final var config = SeriesConfig.builder().name("ScbRegionalStatistics").build();
        final var population = result("population", "TAB6574");

        // when
        when(databasePort.writeScbIndicatorResult(population)).thenThrow(new RuntimeException("connection reset"));
        final var scrapeResult = new ScbRegionalPersister(databasePort).persist(input(config, List.of(population)));

        // then
        assertEquals(ScrapeResult.FAILED, scrapeResult);
        verify(databasePort).writeScbTableStamps(Map.of(), Set.of("TAB6574"));
        verify(databasePort, never()).markAsDone(any());
    }

    @Test
    void shouldReturnEmpty_andStillMarkAsDone_whenNothingChanged() {
        // given
        final var config = SeriesConfig.builder().name("ScbRegionalStatistics").build();

        // when
        final var scrapeResult = new ScbRegionalPersister(databasePort).persist(input(config, List.of()));

        // then
        assertEquals(ScrapeResult.EMPTY, scrapeResult);
        verify(databasePort).markAsDone(config);
        verify(databasePort, never()).writeScbIndicatorResult(any());
        verify(databasePort, never()).writeScbTableStamps(any(), any());
    }

    private static ScbIndicatorResult result(String id, String table) {
        final var source = new ScbSource(table, Map.of());

        return ScbIndicatorResult.builder()
                .indicator(ScbIndicator.builder().id(id).numerator(List.of(source)).denominator(List.of()).build())
                .tableStamps(Map.of(table, UPDATED))
                .values(List.of())
                .build();
    }

    private static ConfigWithScbIndicators input(SeriesConfig config, List<ScbIndicatorResult> results) {
        return input(config, results, Set.of());
    }

    private static ConfigWithScbIndicators input(
            SeriesConfig config,
            List<ScbIndicatorResult> results,
            Set<String> failedTables
    ) {
        return new ConfigWithScbIndicators(config, List.of(), Map.of(), Set.of(), results, failedTables);
    }

}
