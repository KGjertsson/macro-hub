package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExistingScbTableFinderTest {

    @Mock
    DatabasePort databasePort;

    @Test
    void shouldLoadIndicatorsStampsAndIndicatorsWithData() {
        // given
        final var config = SeriesConfig.builder().name("ScbRegionalStatistics").build();
        final var indicator = ScbIndicator.builder().id("population").build();
        final var stamps = Map.of("TAB6574", Instant.parse("2026-03-24T07:00:00Z"));

        // when
        when(databasePort.readScbIndicators()).thenReturn(List.of(indicator));
        when(databasePort.readScbTableStamps()).thenReturn(stamps);
        when(databasePort.readScbIndicatorsWithData()).thenReturn(Set.of("population"));
        final var result = new ExistingScbTableFinder(databasePort).findExisting(config).orElseThrow();

        // then
        assertEquals(config, result.seriesConfig());
        assertEquals(List.of(indicator), result.indicators());
        assertEquals(stamps, result.tableStamps());
        assertEquals(Set.of("population"), result.indicatorsWithData());
        assertEquals(List.of(), result.results());
        assertEquals(Set.of(), result.failedTables());
    }

}
