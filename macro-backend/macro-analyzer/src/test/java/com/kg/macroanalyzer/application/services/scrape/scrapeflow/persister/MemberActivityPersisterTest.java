package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberActivity;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.ScrapeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberActivityPersisterTest {

    MemberActivityPersister persister;

    @Mock
    DatabasePort databasePort;
    @Mock
    ScrapeQueuePort scrapeQueuePort;

    @BeforeEach
    void setUp() {
        persister = new MemberActivityPersister(databasePort, scrapeQueuePort);
    }

    @Test
    void shouldReturnEmpty_whenNoActivityScraped() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        final var result = persister.persist(new ConfigWithMemberActivity(seriesConfig, List.of(), List.of()));

        // then
        assertEquals(ScrapeResult.EMPTY, result);
        verify(scrapeQueuePort).markAsDone(seriesConfig);
        verify(databasePort, never()).writeMemberActivity(any());
    }

    @Test
    void shouldUpsertActivity_whenActivityScraped() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var activity = List.of(MemberActivity.builder().intressentId("1").votesYes(10).build());

        // when
        when(databasePort.writeMemberActivity(activity)).thenReturn(1);
        final var result = persister.persist(new ConfigWithMemberActivity(seriesConfig, List.of("1"), activity));

        // then
        assertEquals(ScrapeResult.SUCCESS, result);
        verify(scrapeQueuePort).markAsDone(seriesConfig);
        verify(databasePort).writeMemberActivity(activity);
    }

}
