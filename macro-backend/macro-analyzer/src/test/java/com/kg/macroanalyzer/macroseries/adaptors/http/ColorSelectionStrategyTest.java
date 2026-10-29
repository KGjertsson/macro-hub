package com.kg.macroanalyzer.macroseries.adaptors.http;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ColorSelectionStrategyTest {

    private final ColorSelectionStrategy colorSelectionStrategy = new ColorSelectionStrategy();

    @Test
    public void shouldCycleThroughPalette_whenMoreSeriesThanColors() {
        // when
        final var colors = IntStream.range(0, 12)
                .mapToObj(colorSelectionStrategy::pickColor)
                .toList();

        // then
        assertEquals(colors.get(0), colors.get(5));
        assertEquals(colors.get(1), colors.get(6));
        assertEquals(colors.get(0), colors.get(10));
        IntStream.range(1, 5).forEach(i -> assertNotEquals(colors.get(0), colors.get(i)));
    }

}
