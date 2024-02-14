package com.kg.macroanalyzer.application;


import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LabelGeneratorTest {

    LabelGenerator labelGenerator;

    @BeforeEach
    public void setUp() {
        labelGenerator = new LabelGenerator();
    }

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
        final var bundle = MacroBundle.builder()
                .macroSeries(List.of(seriesOne, seriesTwo))
                .build();

        // when
        final var paddedBundleOptional = labelGenerator.padToFullLabels(bundle);

        // then
        assertTrue(paddedBundleOptional.isPresent());
        paddedBundleOptional.ifPresent(paddedBundle -> {
            final var labels = paddedBundle.macroSeries()
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
        final var bundle = MacroBundle.builder()
                .macroSeries(List.of())
                .build();

        // when
        final var fullLabelsOptional = labelGenerator.padToFullLabels(bundle);

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
