package com.kg.macroanalyzer.macroseries.application.service.bundleformat;

import com.kg.macroanalyzer.macroseries.application.port.in.Strategy;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

public class AlignmentTest {

    @Test
    public void alignBundle_withSimpleBundle() {
        // given
        final var macroSeries = List.of(
                MacroSeries.builder()
                        .name("a")
                        .macroPoints(List.of(
                                MacroPoint.builder().value(1.0).date(LocalDate.of(2024, 1, 31)).build(),
                                MacroPoint.builder().value(2.0).date(LocalDate.of(2024, 2, 1)).build()
                        ))
                        .build()
        );

        // when
        final var result = Alignment.align(macroSeries, Strategy.MONTH).orElseThrow();

        // then
        assertEquals(List.of(YearMonth.of(2024, 1), YearMonth.of(2024, 2)), result.labels());
        final var points = result.macroSeries().getFirst().macroPoints();
        assertEquals(List.of(1.0, 2.0), points.stream().map(MacroPoint::value).toList());
    }

    @Test
    public void alignBundle_withNullBundle() {
        // when
        final var result = Alignment.align(null, Strategy.MONTH);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldThrowException_whenStrategyIsNull() {
        // when/then
        assertThrows(
                IllegalArgumentException.class,
                () -> Alignment.align(emptyList(), null)
        );
    }

}
