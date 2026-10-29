package com.kg.macroanalyzer.macroseries.adaptors.web;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class MacroPointWebAdaptorTest {

    MacroPointWebAdaptor macroPointWebAdaptor;

    @Mock
    MacroPointAdaptorFlowFactory webAdaptorFlowFactory;

    @Mock
    WebAdaptorFlow<SeriesConfig, Stream<MacroPoint>> webAdaptorFlow;

    @BeforeEach
    public void setUp() {
        this.macroPointWebAdaptor = new MacroPointWebAdaptor(webAdaptorFlowFactory);
    }

    @Test
    void shouldReturnMacroPointStream_whenFetchIsSuccessful() throws ScrapeException {
        // given
        final var seriesConfig = SeriesConfig.builder().name("TEST").build();
        final var mockMacroPoint = MacroPoint.builder()
                .value(1.0)
                .date(java.time.LocalDate.now())
                .build();

        // when
        when(webAdaptorFlowFactory.build(seriesConfig)).thenReturn(webAdaptorFlow);
        when(webAdaptorFlow.fetch(seriesConfig)).thenReturn(Stream.of(mockMacroPoint));
        final var result = macroPointWebAdaptor.fetch(seriesConfig).toList();

        // then
        assertEquals(1, result.size());
        assertEquals(result.getFirst(), mockMacroPoint);
    }

    @Test
    void shouldThrowScrapeException_whenFetchThrowsScrapeException() throws ScrapeException {
        // given
        final var seriesConfig = SeriesConfig.builder().name("TEST").build();

        // when
        when(webAdaptorFlowFactory.build(seriesConfig)).thenReturn(webAdaptorFlow);
        when(webAdaptorFlow.fetch(seriesConfig)).thenThrow(new ScrapeException("Fetch error"));

        // then
        assertThrows(ScrapeException.class, () -> macroPointWebAdaptor.fetch(seriesConfig));
    }

}
