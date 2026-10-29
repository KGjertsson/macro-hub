package com.kg.macroanalyzer.scrape.application.service.enqueue;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.scrape.application.port.in.EnqueueException;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnqueueServiceTest {

    EnqueueService enqueueService;

    @Mock
    ScrapeQueuePort scrapeQueuePort;

    @BeforeEach
    public void setUp() {
        final var seriesConfigList = TestSeriesConfigFactory.buildTestConfig();
        enqueueService = new EnqueueService(seriesConfigList, scrapeQueuePort);
    }

    @ParameterizedTest
    @NullSource
    public void enqueueShouldThrowEnqueueException_whenNullName(String name) {
        // when
        assertThrows(
                EnqueueException.class,
                () -> enqueueService.enqueue(name)
        );
    }

    @Test
    public void enqueueShouldPersistSingleItem_whenNameMatchesConfig() throws EnqueueException {
        // given
        final var name = "macro_series_1";

        // when
        doNothing().when(scrapeQueuePort).persistScrapeQueueItem(any());
        enqueueService.enqueue(name);

        // then
        verify(scrapeQueuePort).persistScrapeQueueItem(any());
    }

    @Test
    public void enqueueShouldPersistNothing_whenNoConfigMatched() throws EnqueueException {
        // given
        final var name = "macro_series_999";

        // when
        enqueueService.enqueue(name);

        // then
        verifyNoInteractions(scrapeQueuePort);
    }

    @Test
    public void shouldPersistAllWithBackoff_whenEnqueueAllWithNoQueue() {
        // when
        when(scrapeQueuePort.getScrapeQueue()).thenReturn(emptyList());
        enqueueService.enqueueAll();

        // then
        verify(scrapeQueuePort, times(3)).persistScrapeQueueItem(any());
    }

    @Test
    public void shouldFilterOutExistingQueueItems_whenQueueIsNotEmpty() {
        // given
        final var existingQueue = TestSeriesConfigFactory.buildTestConfig();
        final var scrapeQueueItems = existingQueue.stream()
                .filter(c -> !c.name().equals("macro_series_1"))
                .map(c -> new ScrapeQueueItem(null, c.name(), Instant.now(), ScrapeQueueKind.MACRO_POINT))
                .toList();

        // when
        when(scrapeQueuePort.getScrapeQueue()).thenReturn(scrapeQueueItems);
        enqueueService.enqueueAll();

        // then
        verify(scrapeQueuePort, times(1)).persistScrapeQueueItem(any());
    }

}
