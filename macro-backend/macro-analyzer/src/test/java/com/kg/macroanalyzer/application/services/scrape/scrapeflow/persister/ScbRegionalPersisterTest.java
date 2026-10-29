package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicatorResult;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithScbIndicators;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScbRegionalPersisterTest {

    @Mock
    DatabasePort databasePort;

    @Test
    void shouldWriteEveryIndicator_andMarkAsDone() {
        // given
        final var config = SeriesConfig.builder().name("ScbRegionalStatistics").build();
        final var population = result("population");
        final var households = result("households");

        // when
        when(databasePort.writeScbIndicatorResult(any())).thenReturn(1);
        final var scrapeResult = new ScbRegionalPersister(databasePort).persist(input(config, List.of(population, households)));

        // then
        assertEquals(ScrapeResult.SUCCESS, scrapeResult);
        verify(databasePort).markAsDone(config);
        verify(databasePort).writeScbIndicatorResult(population);
        verify(databasePort).writeScbIndicatorResult(households);
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
    }

    private static ScbIndicatorResult result(String id) {
        return ScbIndicatorResult.builder()
                .indicator(ScbIndicator.builder().id(id).build())
                .tableStamps(Map.of())
                .values(List.of())
                .build();
    }

    private static ConfigWithScbIndicators input(SeriesConfig config, List<ScbIndicatorResult> results) {
        return new ConfigWithScbIndicators(config, List.of(), Map.of(), Set.of(), results);
    }

}
