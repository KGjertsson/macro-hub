package com.kg.macroanalyzer.application;


import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.application.services.LabelGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class LabelGenerationServiceTest {

    @InjectMocks
    LabelGenerationService labelGenerationService;

    TestJsonReader testJsonReader;

    @BeforeEach
    public void setUp() {
        testJsonReader = new TestJsonReader();
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_macro_series.json"})
    public void generateLabels_findExtremesWithMultipleSeries(String resource) {
        // given
        final var expectedLabelSize = 4;
        final var expectedStartDate = LocalDate.of(2024, 1, 1);
        final var expectedEndDate = LocalDate.of(2024, 1, 4);
        final var macroSeriesList = testJsonReader.readMacroSeriesList(resource);

        // when
        final var paddedSeriesOptional = labelGenerationService.padToFullLabels(macroSeriesList);

        // then
        assertTrue(paddedSeriesOptional.isPresent());
        paddedSeriesOptional.ifPresent(paddedSeries -> {
            final var labels = paddedSeries
                    .getFirst()
                    .macroPoints().stream()
                    .map(MacroPoint::date).toList();
            final var values = paddedSeries
                    .getFirst()
                    .macroPoints().stream()
                    .map(MacroPoint::value).toList();
            assertEquals(expectedLabelSize, labels.size());
            assertEquals(expectedStartDate, labels.getFirst());
            assertEquals(expectedEndDate, labels.getLast());
            assertTrue(values.getLast() > 0);
        });
    }

    @Test
    public void generateLabels_withEmptySeries() {
        // given
        final var emptyList = List.<MacroSeries>of();
        // when
        final var fullLabelsOptional = labelGenerationService.padToFullLabels(emptyList);

        // then
        assertTrue(fullLabelsOptional.isEmpty());
    }

}
