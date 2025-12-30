package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MacroPointPersisterTest {

    MacroPointPersister macroPointPersister;

    TestJsonReader testJsonReader;

    @Mock
    DatabasePort databasePort;

    @BeforeEach
    void setUp() {
        macroPointPersister = new MacroPointPersister(databasePort);
        testJsonReader = new TestJsonReader();
    }

    @Test
    void shouldReturnEmpty_whenNoMacroPointsFound() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var configWithMacroPoints = ConfigWithMacroPoints.builder()
                .seriesConfig(seriesConfig)
                .macroPoints(emptyList())
                .build();

        // when
        final var result = macroPointPersister.persist(configWithMacroPoints);

        // then
        verify(databasePort).markAsDone(seriesConfig);
        assertEquals(ScrapeResult.EMPTY, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/single_macro_series.json"})
    void shouldReturnSuccess_whenMacroPointsFound(String resource) {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var macroSeries = testJsonReader.readMacroSeriesList(resource).getFirst();
        final var configWithMacroPoints = ConfigWithMacroPoints.builder()
                .seriesConfig(seriesConfig)
                .macroPoints(macroSeries.macroPoints())
                .build();

        // when
        when(databasePort.writeMacroPoints(any())).thenReturn(macroSeries.macroPoints().size());
        final var result = macroPointPersister.persist(configWithMacroPoints);

        // then
        verify(databasePort).markAsDone(seriesConfig);
        verify(databasePort).writeMacroPoints(configWithMacroPoints);
        assertEquals(ScrapeResult.SUCCESS, result);
    }

}
