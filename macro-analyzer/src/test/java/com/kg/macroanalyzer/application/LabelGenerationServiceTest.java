package com.kg.macroanalyzer.application;


import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.services.LabelGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    public void generateLabels_findExtremesWithMultipleSeries() {
        // given
        final var expectedLabelSize = 4;
        final var expectedStartDate = LocalDate.of(2024, 1, 1);
        final var expectedEndDate = LocalDate.of(2024, 1, 4);
        final var resource = "json/macro_series_multiple.json";
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
            assertEquals(expectedLabelSize, labels.size());
            assertEquals(expectedStartDate, labels.getFirst());
            assertEquals(expectedEndDate, labels.getLast());
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
