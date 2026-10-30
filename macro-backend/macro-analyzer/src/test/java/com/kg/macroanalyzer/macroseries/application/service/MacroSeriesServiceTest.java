package com.kg.macroanalyzer.macroseries.application.service;

import com.kg.macroanalyzer.macroseries.application.port.in.BuildChartDataParams;
import com.kg.macroanalyzer.macroseries.application.port.in.Strategy;
import com.kg.macroanalyzer.macroseries.application.port.in.TimeFrame;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.AlignmentService;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.TimeFilterService;
import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import com.kg.macroanalyzer.macroseries.domain.MacroSeriesScrapeFlows;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MacroSeriesServiceTest {

    MacroSeriesService macroSeriesService;

    @Mock
    MacroSeriesRepositoryPort repositoryPort;
    @Mock
    AlignmentService alignmentService;
    @Mock
    TimeFilterService timeFilterService;

    @BeforeEach
    void setUp() {
        macroSeriesService = new MacroSeriesService(repositoryPort, emptyList(), alignmentService, timeFilterService);
    }

    @Test
    void shouldReturnAlignedBundle_whenInputListIsPresent() {
        // given
        final var firstDate = LocalDate.of(2024, 1, 1);
        final var secondDate = LocalDate.of(2024, 1, 1);
        final var macroSeriesList = List.of(
                new MacroSeries("seriesA", List.of(
                        new MacroPoint(1.0, firstDate),
                        new MacroPoint(2.0, secondDate))
                )
        );
        final var alignedBundle = AlignedBundle.builder()
                .labels(List.of(firstDate, secondDate))
                .macroSeries(macroSeriesList)
                .build();
        when(repositoryPort.readMacroSeries(anyList())).thenReturn(macroSeriesList);
        when(timeFilterService.shrinkToTimeFrame(anyList(), any())).thenReturn(Optional.of(macroSeriesList));
        when(alignmentService.align(any(), any())).thenReturn(Optional.of(alignedBundle));
        final var params = BuildChartDataParams.builder()
                .chartSeriesParams(emptyList())
                .strategy(Strategy.MONTH)
                .timeFrame(TimeFrame.ALL)
                .build();

        // when

        final var response = macroSeriesService.buildAlignedBundle(params);

        // then
        assertTrue(response.isPresent());
        response.ifPresent(r -> {
            assertEquals(2, r.labels().size());
            assertEquals(2, r.macroSeries().getFirst().macroPoints().size());
        });

    }

    @Test
    void shouldReturnEmpty_whenInputListIsEmpty() {
        // given
        final var params = BuildChartDataParams.builder()
                .chartSeriesParams(emptyList())
                .strategy(Strategy.MONTH)
                .build();
        when(repositoryPort.readMacroSeries(anyList())).thenReturn(emptyList());

        // when
        final var response = macroSeriesService.buildAlignedBundle(params);

        // then
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldKeepConfigsOfOtherScrapeFlowsOutOfChartList() {
        // given
        final var rate = SeriesConfig.builder().name("PolicyRateSweden").scrapeFlow(MacroSeriesScrapeFlows.MACRO_POINT).build();
        final var members = SeriesConfig.builder().name("MembersOfParliament").scrapeFlow("MEMBER_OF_PARLIAMENT").build();
        final var elections = SeriesConfig.builder().name("MunicipalElections").scrapeFlow("LOCAL_ELECTION").build();
        final var scb = SeriesConfig.builder().name("ScbRegionalStatistics").scrapeFlow("SCB_REGIONAL").build();
        final var service = new MacroSeriesService(
                repositoryPort, List.of(rate, members, elections, scb), alignmentService, timeFilterService);

        // when
        final var chartList = service.getChartSeriesConfigList();

        // then
        assertEquals(List.of(rate), chartList);
    }

}
