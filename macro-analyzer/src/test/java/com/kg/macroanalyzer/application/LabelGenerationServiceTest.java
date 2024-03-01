package com.kg.macroanalyzer.application;


import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.services.LabelGenerationService;
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

    @Test
    public void generateLabels_findExtremesWithMultipleSeries() {
        // given
        final var expectedLabelSize = 4;
        final var expectedStartDate = LocalDate.of(2024, 1, 1);
        final var expectedEndDate = LocalDate.of(2024, 1, 4);
        final var seriesOne = MacroSeries.builder()
                .name("a")
                .macroPoints(List.of(
                        buildPoint(expectedStartDate),
                        buildPoint(LocalDate.of(2024, 1, 2))
                )).build();
        final var seriesTwo = MacroSeries.builder()
                .name("a")
                .macroPoints(List.of(
                        buildPoint(LocalDate.of(2024, 1, 2)),
                        buildPoint(expectedEndDate)
                )).build();
        final var macroSeriesList = List.of(seriesOne, seriesTwo);

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

    private MacroPoint buildPoint(LocalDate localDate) {
        return MacroPoint.builder()
                .value(1.0)
                .date(localDate)
                .build();
    }

}
