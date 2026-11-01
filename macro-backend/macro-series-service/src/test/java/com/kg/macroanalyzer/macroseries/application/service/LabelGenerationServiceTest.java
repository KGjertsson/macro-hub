package com.kg.macroanalyzer.macroseries.application.service;


import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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
            // macro_series_1 ends on 2024-01-02, so later labels have no observation
            assertEquals(Arrays.asList(11.0, 12.0, null, null), values);
        });
    }

    @Test
    public void generateLabels_padsWithNullOutsideSeriesRange_andCarriesValueOverOwnGaps() {
        // given
        final var wide = series("wide", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 6));
        final var narrow = series("narrow", LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 4));

        // when
        final var padded = labelGenerationService.padToFullLabels(List.of(wide, narrow)).orElseThrow();

        // then
        assertEquals(Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0, 2.0), padded.get(0).getValues());
        assertEquals(Arrays.asList(null, 1.0, 1.0, 2.0, null, null), padded.get(1).getValues());
    }

    @Test
    public void generateLabels_padsSeriesWithoutPointsWithNull() {
        // given
        final var withData = series("withData", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2));
        final var withoutData = MacroSeries.builder().name("withoutData").macroPoints(List.of()).build();

        // when
        final var padded = labelGenerationService.padToFullLabels(List.of(withData, withoutData)).orElseThrow();

        // then
        assertEquals(Arrays.asList(null, null), padded.get(1).getValues());
        assertEquals(
                padded.get(0).macroPoints().stream().map(MacroPoint::date).toList(),
                padded.get(1).macroPoints().stream().map(MacroPoint::date).toList()
        );
    }

    private static MacroSeries series(String name, LocalDate... dates) {
        final var points = IntStream.range(0, dates.length)
                .mapToObj(i -> MacroPoint.builder().value(i + 1.0).date(dates[i]).build())
                .toList();

        return MacroSeries.builder().name(name).macroPoints(points).build();
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
