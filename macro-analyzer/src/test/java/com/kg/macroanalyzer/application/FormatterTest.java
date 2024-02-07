package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FormatterTest {

    @InjectMocks
    Formatter formatter;
    @Mock
    MacroSamplerStrategy macroSamplerStrategy;
    @Mock
    LabelGenerator labelGenerator;


    @Test
    public void alignBundle_withSimpleBundle() {
        // given
        final var inputBundle = MacroBundle.builder()
                .macroSeries(List.of(
                        MacroSeries.builder()
                                .name("a")
                                .macroPoints(emptyPoints())
                                .build())
                )
                .build();
        final var withLabels = Formatter.WithFullLabels.builder()
                .macroBundle(inputBundle)
                .labels(List.of()).build();

        // when
        when(labelGenerator.padToFullLabels(inputBundle)).thenReturn(Optional.of(withLabels));
        when(macroSamplerStrategy.sample(inputBundle)).thenReturn(Optional.of(inputBundle));
        final var result = formatter.align(inputBundle);

        // then
        assertTrue(result.isPresent());
        result.ifPresent(r -> assertEquals(r, inputBundle));
    }

    @Test
    public void alignBundle_withNullBundle() {
        // when
        final var result = formatter.align(null);

        // then
        assertTrue(result.isEmpty());
    }

    private List<MacroPoint> emptyPoints() {
        return IntStream.range(0, 1)
                .mapToObj(Double::valueOf)
                .map(n -> MacroPoint.builder().value(n).date(LocalDate.MIN).build())
                .toList();
    }

}
