package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.adaptors.web.WebAdaptor;
import com.kg.macroanalyzer.adaptors.web.macroseries.WebAdaptorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScrapeUtilsTest {

    @InjectMocks
    ScrapeUtils scrapeUtils;
    @Mock
    WebAdaptorFactory webAdaptorFactory;
    @Mock
    WebAdaptor webAdaptor;
    @Mock
    SeriesConfig seriesConfig;

    @ParameterizedTest
    @NullSource
    public void shouldThrowScrapeException_whenNullConfig(SeriesConfig seriesConfig) {
        // given
        final List<MacroPoint> macroPoints = emptyList();

        // when/then
        assertThrows(
                ScrapeException.class,
                () -> scrapeUtils.scrapeNovelItems(seriesConfig, macroPoints)
        );
    }

    @Test
    public void shouldThrowScrapeException_whenWebUtilsThrows() throws ScrapeException {
        // given
        final List<MacroPoint> macroPoints = emptyList();

        // when
        when(webAdaptorFactory.build(any())).thenReturn(webAdaptor);
        when(webAdaptor.fetchSeriesData(any())).thenThrow(new ScrapeException("Test ScrapeException"));

        // when/then
        assertThrows(
                ScrapeException.class,
                () -> scrapeUtils.scrapeNovelItems(seriesConfig, macroPoints)
        );
    }

    @Test
    public void shouldReturnFullScrapeList_whenPersistedListIsEmpty() throws ScrapeException {
        // given
        final List<MacroPoint> persistedList = emptyList();
        final var scrapedList = Stream.of(
                new MacroPoint(1.0, LocalDate.of(2024, 1, 1)),
                new MacroPoint(2.0, LocalDate.of(2024, 1, 2)),
                new MacroPoint(3.0, LocalDate.of(2024, 1, 3))
        );

        // when
        when(webAdaptorFactory.build(any())).thenReturn(webAdaptor);
        when(webAdaptor.fetchSeriesData(any())).thenReturn(scrapedList);
        final var novelItems = scrapeUtils.scrapeNovelItems(seriesConfig, persistedList);

        // then
        assertEquals(3, novelItems.size());
    }

    @Test
    public void shouldReturnFilteredScrapeList_whenPersistedListIsCollidesWithScrapedItems() throws ScrapeException {
        // given
        final var persistedList = List.of(
                new MacroPoint(1.0, LocalDate.of(2024, 1, 1)),
                new MacroPoint(2.0, LocalDate.of(2024, 1, 2))
        );
        final var scrapedList = Stream.of(
                new MacroPoint(1.0, LocalDate.of(2024, 1, 1)),
                new MacroPoint(2.0, LocalDate.of(2024, 1, 2)),
                new MacroPoint(3.0, LocalDate.of(2024, 1, 3))
        );

        // when
        when(webAdaptorFactory.build(any())).thenReturn(webAdaptor);
        when(webAdaptor.fetchSeriesData(any())).thenReturn(scrapedList);
        final var novelItems = scrapeUtils.scrapeNovelItems(seriesConfig, persistedList);

        // then
        assertEquals(1, novelItems.size());
    }

}
