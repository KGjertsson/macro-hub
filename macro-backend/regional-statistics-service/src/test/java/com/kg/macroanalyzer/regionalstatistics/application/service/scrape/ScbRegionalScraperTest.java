package com.kg.macroanalyzer.regionalstatistics.application.service.scrape;

import com.kg.macroanalyzer.regionalstatistics.application.port.out.ConfigWithScbIndicators;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.ScbDataRequest;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.ScbMetadataRequest;
import com.kg.macroanalyzer.regionalstatistics.domain.GeoVersion;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbAreaValue;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicator;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbLevel;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbSource;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableMetadata;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableValues;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScbRegionalScraperTest {

    private static final String BASE_URL = "https://statistikdatabasen.scb.se/api/v2";
    private static final Instant UPDATED = Instant.parse("2026-03-24T07:00:00Z");
    private static final Instant OLD = Instant.parse("2025-03-24T07:00:00Z");
    private static final ScbSource TOTAL = source("TAB6574", "totalt");
    private static final ScbSource OLDER_65 = source("TAB6574", "65-69", "70-74");

    ScbRegionalScraper scraper;

    @Mock
    WebPort<ScbMetadataRequest, ScbTableMetadata> metadataWebPort;
    @Mock
    WebPort<ScbDataRequest, ScbTableValues> dataWebPort;

    @BeforeEach
    void setUp() {
        scraper = new ScbRegionalScraper(metadataWebPort, dataWebPort, 150_000);
    }

    @Test
    void shouldSkipIndicator_whenItsTablesAreUnchangedAndItHasData() throws ScrapeException {
        // given
        final var population = indicator("population", List.of(TOTAL), List.of(), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(new ScbMetadataRequest(BASE_URL, "TAB6574"))).thenReturn(metadata("TAB6574", UPDATED));
        final var result = scraper.scrape(input(List.of(population), Map.of("TAB6574", UPDATED), Set.of("population")));

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().results());
        verify(dataWebPort, never()).fetch(any());
    }

    @Test
    void shouldFetchIndicator_whenTablesAreUnchangedButItHasNoData() throws ScrapeException {
        // given
        final var population = indicator("population", List.of(TOTAL), List.of(), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(any())).thenReturn(metadata("TAB6574", UPDATED));
        when(dataWebPort.fetch(any())).thenReturn(values(Map.of("0114A0010", 1000.0)));
        final var result = scraper.scrape(input(List.of(population), Map.of("TAB6574", UPDATED), Set.of()));

        // then
        final var indicatorResult = result.orElseThrow().results().getFirst();
        assertEquals(List.of(new ScbAreaValue(2024, GeoVersion.DESO_2018, "0114A0010", 1000.0)), indicatorResult.values());
        assertEquals(Map.of("TAB6574", UPDATED), indicatorResult.tableStamps());
    }

    @Test
    void shouldFetchEachVersionOnce_andReuseASharedDenominator() throws ScrapeException {
        // given
        final var population = indicator("population", List.of(TOTAL), List.of(), ScbLevel.DESO);
        final var share65 = indicator("share_65_plus", List.of(OLDER_65), List.of(TOTAL), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(any())).thenReturn(metadata("TAB6574", UPDATED));
        when(dataWebPort.fetch(any())).thenAnswer(invocation -> {
            final ScbDataRequest request = invocation.getArgument(0);
            final var older = request.selection().get("Alder").size() > 1;
            return request.regionCodes().equals(GeoVersion.DESO_2018.wildcard())
                    ? values(Map.of("0114A0010", older ? 250.0 : 1000.0))
                    : values(Map.of("0114A0010_DeSO2025", older ? 300.0 : 1200.0));
        });
        final var result = scraper.scrape(input(List.of(population, share65), Map.of("TAB6574", OLD), Set.of("population")));

        // then
        final var results = result.orElseThrow().results();
        assertEquals(2, results.size());
        assertEquals(
                Set.of(
                        new ScbAreaValue(2024, GeoVersion.DESO_2018, "0114A0010", 25.0),
                        new ScbAreaValue(2024, GeoVersion.DESO_2025, "0114A0010", 25.0)
                ),
                new HashSet<>(results.get(1).values())
        );
        // population total and the 65+ groups, each for DeSO 2018 and DeSO 2025
        verify(dataWebPort, times(4)).fetch(any());
        verify(metadataWebPort, times(1)).fetch(any());
    }

    @Test
    void shouldLetTheLaterSourceWin_andChunkYearsByCellLimit() throws ScrapeException {
        // given
        final var oldTable = source("TAB6091", "totalt");
        final var newTable = source("TAB6589", "totalt");
        final var cars = indicator("cars", List.of(oldTable, newTable), List.of(), ScbLevel.REGSO);
        scraper = new ScbRegionalScraper(metadataWebPort, dataWebPort, 1);

        // when
        when(metadataWebPort.fetch(new ScbMetadataRequest(BASE_URL, "TAB6091"))).thenReturn(metadata("TAB6091", UPDATED));
        when(metadataWebPort.fetch(new ScbMetadataRequest(BASE_URL, "TAB6589"))).thenReturn(metadata("TAB6589", UPDATED));
        when(dataWebPort.fetch(any())).thenAnswer(invocation -> {
            final ScbDataRequest request = invocation.getArgument(0);
            final var value = request.table().equals("TAB6091") ? 1.0 : 2.0;
            return request.regionCodes().equals(GeoVersion.REGSO_2020.wildcard())
                    ? values(Map.of("0114R001", value))
                    : values(Map.of());
        });
        final var result = scraper.scrape(input(List.of(cars), Map.of(), Set.of()));

        // then
        assertEquals(
                List.of(new ScbAreaValue(2024, GeoVersion.REGSO_2020, "0114R001", 2.0)),
                result.orElseThrow().results().getFirst().values()
        );
        // One RegSO 2020 area and a limit of 1 cell: one call per year and table; no RegSO 2025 codes, no call
        verify(dataWebPort, times(4)).fetch(any());
    }

    @Test
    void shouldSkipAFailedIndicator_andReportTheTableThatFailed() throws ScrapeException {
        // given
        final var population = indicator("population", List.of(TOTAL), List.of(), ScbLevel.DESO);
        final var share65 = indicator("share_65_plus", List.of(OLDER_65), List.of(TOTAL), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(any())).thenReturn(metadata("TAB6574", UPDATED));
        when(dataWebPort.fetch(any())).thenAnswer(invocation -> {
            final ScbDataRequest request = invocation.getArgument(0);
            if (request.selection().get("Alder").size() > 1) {
                throw new ScrapeException("HTTP 429");
            }
            return values(Map.of("0114A0010", 1000.0));
        });
        final var result = scraper.scrape(input(List.of(population, share65), Map.of(), Set.of()));

        // then
        final var results = result.orElseThrow().results();
        assertEquals(List.of("population"), results.stream().map(r -> r.indicator().id()).toList());
        assertEquals(Map.of("TAB6574", UPDATED), results.getFirst().tableStamps());
        assertEquals(Set.of("TAB6574"), result.get().failedTables());
    }

    @Test
    void shouldReportOnlyTheFailedTable_notEveryTableOfTheFailedIndicator() throws ScrapeException {
        // given
        final var population = indicator("population", List.of(TOTAL), List.of(), ScbLevel.DESO);
        final var cars = indicator("cars_per_1000", List.of(source("TAB6589", "totalt")), List.of(TOTAL), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(new ScbMetadataRequest(BASE_URL, "TAB6574"))).thenReturn(metadata("TAB6574", UPDATED));
        when(metadataWebPort.fetch(new ScbMetadataRequest(BASE_URL, "TAB6589"))).thenReturn(metadata("TAB6589", UPDATED));
        when(dataWebPort.fetch(any())).thenAnswer(invocation -> {
            final ScbDataRequest request = invocation.getArgument(0);
            if (request.table().equals("TAB6589")) {
                throw new ScrapeException("HTTP 404");
            }
            return values(Map.of("0114A0010", 1000.0));
        });
        final var result = scraper.scrape(input(List.of(population, cars), Map.of(), Set.of()));

        // then
        assertEquals(List.of("population"), result.orElseThrow().results().stream().map(r -> r.indicator().id()).toList());
        assertEquals(Set.of("TAB6589"), result.get().failedTables());
        assertEquals(Map.of("TAB6574", UPDATED), result.get().results().getFirst().tableStamps());
    }

    @Test
    void shouldReportTheTable_whoseMetadataFailed() throws ScrapeException {
        // given
        final var population = indicator("population", List.of(TOTAL), List.of(), ScbLevel.DESO);
        final var cars = indicator("cars_per_1000", List.of(source("TAB6589", "totalt")), List.of(TOTAL), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(new ScbMetadataRequest(BASE_URL, "TAB6574"))).thenReturn(metadata("TAB6574", UPDATED));
        when(metadataWebPort.fetch(new ScbMetadataRequest(BASE_URL, "TAB6589"))).thenThrow(new ScrapeException("HTTP 404"));
        when(dataWebPort.fetch(any())).thenReturn(values(Map.of("0114A0010", 1000.0)));
        final var result = scraper.scrape(input(List.of(population, cars), Map.of(), Set.of()));

        // then
        assertEquals(Set.of("TAB6589"), result.orElseThrow().failedTables());
        assertEquals(1, result.get().results().size());
    }

    @Test
    void shouldFail_whenEveryIndicatorFails() throws ScrapeException {
        // given
        final var population = indicator("population", List.of(TOTAL), List.of(), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(any())).thenThrow(new ScrapeException("HTTP 404"));
        final var result = scraper.scrape(input(List.of(population), Map.of(), Set.of()));

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFailIndicator_whenADimensionIsNotSelected() throws ScrapeException {
        // given
        final var noAge = new ScbSource("TAB6574", Map.of("ContentsCode", List.of("000007Y7")));
        final var population = indicator("population", List.of(noAge), List.of(), ScbLevel.DESO);

        // when
        when(metadataWebPort.fetch(any())).thenReturn(metadata("TAB6574", UPDATED));
        final var result = scraper.scrape(input(List.of(population), Map.of(), Set.of()));

        // then
        assertTrue(result.isEmpty());
        verify(dataWebPort, never()).fetch(any());
    }

    private static ScbSource source(String table, String... ages) {
        final var selection = new LinkedHashMap<String, List<String>>();
        selection.put("ContentsCode", List.of("000007Y7"));
        selection.put("Alder", List.of(ages));

        return new ScbSource(table, selection);
    }

    private static ScbIndicator indicator(String id, List<ScbSource> numerator, List<ScbSource> denominator, ScbLevel level) {
        return ScbIndicator.builder()
                .id(id)
                .levels(List.of(level))
                .numerator(numerator)
                .denominator(denominator)
                .multiplier(denominator.isEmpty() ? 1 : 100)
                .build();
    }

    private static ScbTableMetadata metadata(String table, Instant updated) {
        final var codes = new LinkedHashMap<String, List<String>>();
        codes.put("Region", List.of("0114", "0114A0010", "0114A0010_DeSO2025", "0114R001"));
        codes.put("Alder", List.of("totalt", "65-69", "70-74"));
        codes.put("ContentsCode", List.of("000007Y7"));
        codes.put("Tid", List.of("2023", "2024"));

        return new ScbTableMetadata(table, updated, codes);
    }

    // Every code has a 0 placeholder in 2023, so only 2024 is kept
    private static ScbTableValues values(Map<String, Double> values2024) {
        final var values = new LinkedHashMap<String, Map<Integer, Double>>();
        values2024.forEach((code, value) -> values.put(code, Map.of(2023, 0.0, 2024, value)));

        return new ScbTableValues(values);
    }

    private static ConfigWithScbIndicators input(
            List<ScbIndicator> indicators,
            Map<String, Instant> stamps,
            Set<String> indicatorsWithData
    ) {
        return ConfigWithScbIndicators.builder()
                .seriesConfig(SeriesConfig.builder().name("ScbRegionalStatistics").scrapeUrl(BASE_URL).build())
                .indicators(indicators)
                .tableStamps(stamps)
                .indicatorsWithData(indicatorsWithData)
                .results(List.of())
                .build();
    }

}
