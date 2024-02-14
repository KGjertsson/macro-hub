package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.SampleStrategy.MacroSamplerStrategyFactory;
import com.kg.macroanalyzer.application.SampleStrategy.SampleStrategy;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BundleFormatterTest {

    @InjectMocks
    BundleFormatter bundleFormatter;
    @Mock
    MacroSamplerStrategyFactory macroSamplerStrategyFactory;
    @Mock
    LabelGenerator labelGenerator;
    @Mock
    SampleStrategy sampleStrategy;


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

        // when
        when(labelGenerator.padToFullLabels(inputBundle)).thenReturn(Optional.of(inputBundle));
        when(macroSamplerStrategyFactory.build()).thenReturn(sampleStrategy);
        when(sampleStrategy.sample(inputBundle)).thenReturn(Optional.of(inputBundle));
        final var result = bundleFormatter.align(inputBundle);

        // then
        assertTrue(result.isPresent());
        result.ifPresent(r -> assertEquals(r, inputBundle));
    }

    @Test
    public void alignBundle_withNullBundle() {
        // given
        when(macroSamplerStrategyFactory.build()).thenReturn(sampleStrategy);

        // when
        final var result = bundleFormatter.align(null);

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
