package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.ScrapeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.kg.macroanalyzer.TestMemberFactory.member;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberOfParliamentPersisterTest {

    MemberOfParliamentPersister persister;

    @Mock
    DatabasePort databasePort;
    @Mock
    ScrapeQueuePort scrapeQueuePort;

    @BeforeEach
    void setUp() {
        persister = new MemberOfParliamentPersister(databasePort, scrapeQueuePort);
    }

    @Test
    void shouldReturnEmpty_whenNoMembersScraped() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        final var result = persister.persist(new ConfigWithMemberOfParliament(seriesConfig, List.of()));

        // then
        assertEquals(ScrapeResult.EMPTY, result);
        verify(scrapeQueuePort).markAsDone(seriesConfig);
        verify(databasePort, never()).writeMembersOfParliament(any());
    }

    @Test
    void shouldUpsertMembers_whenMembersScraped() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var members = List.of(member("1", "S", "Tjänstgörande riksdagsledamot", List.of()));

        // when
        when(databasePort.writeMembersOfParliament(members)).thenReturn(1);
        final var result = persister.persist(new ConfigWithMemberOfParliament(seriesConfig, members));

        // then
        assertEquals(ScrapeResult.SUCCESS, result);
        verify(scrapeQueuePort).markAsDone(seriesConfig);
        verify(databasePort).writeMembersOfParliament(members);
    }

}
