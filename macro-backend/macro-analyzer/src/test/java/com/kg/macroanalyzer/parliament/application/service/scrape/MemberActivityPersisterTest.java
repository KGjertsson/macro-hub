package com.kg.macroanalyzer.parliament.application.service.scrape;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.parliament.application.port.out.ConfigWithMemberActivity;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
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
    ParliamentRepositoryPort repositoryPort;
    @Mock
    ScrapeQueuePort scrapeQueuePort;

    @BeforeEach
    void setUp() {
        persister = new MemberActivityPersister(repositoryPort, scrapeQueuePort);
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
        verify(repositoryPort, never()).writeMemberActivity(any());
    }

    @Test
    void shouldUpsertActivity_whenActivityScraped() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var activity = List.of(MemberActivity.builder().intressentId("1").votesYes(10).build());

        // when
        when(repositoryPort.writeMemberActivity(activity)).thenReturn(1);
        final var result = persister.persist(new ConfigWithMemberActivity(seriesConfig, List.of("1"), activity));

        // then
        assertEquals(ScrapeResult.SUCCESS, result);
        verify(scrapeQueuePort).markAsDone(seriesConfig);
        verify(repositoryPort).writeMemberActivity(activity);
    }

}
