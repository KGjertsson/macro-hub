package com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeriesConfigPreparerTest {

    SeriesConfigPreparer preparer;

    @BeforeEach
    void setUp() {
        final var seriesConfigList = TestSeriesConfigFactory.buildTestConfig();
        preparer = new SeriesConfigPreparer(seriesConfigList);
    }

    @Test
    public void returnEmpty_whenNoSeriesMatch() {
        // given
        final var mismatchingQueueItem = ScrapeQueueItem.of("mismatch");

        // when
        final var seriesConfig = preparer.prepare(mismatchingQueueItem);

        // then
        assertTrue(seriesConfig.isEmpty());
    }

    @Test
    public void returnResult_whenSeriesMatch() {
        // given
        final var mismatchingQueueItem = ScrapeQueueItem.of("macro_series_2");

        // when
        final var seriesConfig = preparer.prepare(mismatchingQueueItem);

        // then
        assertTrue(seriesConfig.isPresent());
    }

}
