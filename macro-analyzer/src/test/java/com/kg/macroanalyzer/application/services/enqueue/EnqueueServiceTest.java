package com.kg.macroanalyzer.application.services.enqueue;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.exceptions.EnqueueException;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
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
    DatabasePort databasePort;

    @BeforeEach
    public void setUp() {
        final var seriesConfigList = TestSeriesConfigFactory.buildTestConfig();
        enqueueService = new EnqueueService(seriesConfigList, databasePort);
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
        doNothing().when(databasePort).persistScrapeQueueItem(any());
        enqueueService.enqueue(name);

        // then
        verify(databasePort).persistScrapeQueueItem(any());
    }

    @Test
    public void enqueueShouldPersistNothing_whenNoConfigMatched() throws EnqueueException {
        // given
        final var name = "macro_series_999";

        // when
        enqueueService.enqueue(name);

        // then
        verifyNoInteractions(databasePort);
    }

    @Test
    public void shouldPersistAllWithBackoff_whenEnqueueAllWithNoQueue() {
        // when
        when(databasePort.getScrapeQueue()).thenReturn(emptyList());
        enqueueService.enqueueAll();

        // then
        verify(databasePort, times(3)).persistScrapeQueueItem(any());
    }

    @Test
    public void shouldFilterOutExistingQueueItems_whenQueueIsNotEmpty() {
        // given
        final var existingQueue = TestSeriesConfigFactory.buildTestConfig();
        final var scrapeQueueItems = existingQueue.stream()
                .filter(c -> !c.name().equals("macro_series_1"))
                .map(c -> new ScrapeQueueItem(null, c.name(), Instant.now()))
                .toList();

        // when
        when(databasePort.getScrapeQueue()).thenReturn(scrapeQueueItems);
        enqueueService.enqueueAll();

        // then
        verify(databasePort, times(1)).persistScrapeQueueItem(any());
    }

}
