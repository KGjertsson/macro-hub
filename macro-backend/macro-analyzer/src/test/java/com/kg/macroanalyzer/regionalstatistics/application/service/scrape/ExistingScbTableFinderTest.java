package com.kg.macroanalyzer.regionalstatistics.application.service.scrape;

import com.kg.macroanalyzer.regionalstatistics.application.port.out.RegionalStatisticsRepositoryPort;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicator;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
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
    RegionalStatisticsRepositoryPort repositoryPort;

    @Test
    void shouldLoadIndicatorsStampsAndIndicatorsWithData() {
        // given
        final var config = SeriesConfig.builder().name("ScbRegionalStatistics").build();
        final var indicator = ScbIndicator.builder().id("population").build();
        final var stamps = Map.of("TAB6574", Instant.parse("2026-03-24T07:00:00Z"));

        // when
        when(repositoryPort.readScbIndicators()).thenReturn(List.of(indicator));
        when(repositoryPort.readScbTableStamps()).thenReturn(stamps);
        when(repositoryPort.readScbIndicatorsWithData()).thenReturn(Set.of("population"));
        final var result = new ExistingScbTableFinder(repositoryPort).findExisting(config).orElseThrow();

        // then
        assertEquals(config, result.seriesConfig());
        assertEquals(List.of(indicator), result.indicators());
        assertEquals(stamps, result.tableStamps());
        assertEquals(Set.of("population"), result.indicatorsWithData());
        assertEquals(List.of(), result.results());
        assertEquals(Set.of(), result.failedTables());
    }

}
