package com.kg.macroanalyzer.macroseries.application.service.scrape;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExistingMacroPointFinderTest {

    ExistingMacroPointFinder existingMacroPointFinder;

    TestJsonReader testJsonReader;

    @Mock
    MacroSeriesRepositoryPort repositoryPort;

    @BeforeEach
    void setUp() {
        existingMacroPointFinder = new ExistingMacroPointFinder(repositoryPort);
        testJsonReader = new TestJsonReader();
    }

    @Test
    void shouldReturnEmpty_whenNoMacroPointsFound() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var emptyMacroSeries = MacroSeries.builder()
                .name(seriesConfig.name())
                .macroPoints(emptyList())
                .build();

        // when
        when(repositoryPort.readMacroSeries(seriesConfig)).thenReturn(emptyMacroSeries);
        final var existing = existingMacroPointFinder.findExisting(seriesConfig);

        // then
        assertTrue(existing.isPresent());
        existing.ifPresent(macroPoints -> assertTrue(macroPoints.macroPoints().isEmpty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/single_macro_series.json"})
    void shouldReturnMacroPoints_whenExistingPointsFound(String resource) {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var macroSeries = testJsonReader.readMacroSeriesList(resource).getFirst();

        // when
        when(repositoryPort.readMacroSeries(seriesConfig)).thenReturn(macroSeries);
        final var existing = existingMacroPointFinder.findExisting(seriesConfig);

        // then
        assertTrue(existing.isPresent());
        existing.map(ConfigWithMacroPoints::macroPoints)
                .ifPresent(macroPoints ->
                        assertEquals(macroSeries.macroPoints().size(), macroPoints.size())
                );

    }

}
