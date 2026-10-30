package com.kg.macroanalyzer.election.application.service.scrape;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalElectionPersisterTest {

    @Mock
    ElectionRepositoryPort repositoryPort;
    @Mock
    ScrapeQueuePort scrapeQueuePort;

    @Test
    void shouldWriteResults_andMarkAsDone() {
        // given
        final var config = SeriesConfig.builder().name("MunicipalElections").build();
        final var results = List.of(LocalElectionResult.builder().areaCode("1281").build());

        // when
        when(repositoryPort.writeLocalElectionResults(results)).thenReturn(1);
        final var scrapeResult = new LocalElectionPersister(repositoryPort, scrapeQueuePort).persist(input(config, results));

        // then
        assertEquals(ScrapeResult.SUCCESS, scrapeResult);
        verify(scrapeQueuePort).markAsDone(config);
    }

    @Test
    void shouldReturnEmpty_andStillMarkAsDone_whenNothingChanged() {
        // given
        final var config = SeriesConfig.builder().name("MunicipalElections").build();

        // when
        final var scrapeResult = new LocalElectionPersister(repositoryPort, scrapeQueuePort).persist(input(config, List.of()));

        // then
        assertEquals(ScrapeResult.EMPTY, scrapeResult);
        verify(scrapeQueuePort).markAsDone(config);
        verify(repositoryPort, never()).writeLocalElectionResults(anyList());
    }

    private static ConfigWithLocalElections input(SeriesConfig config, List<LocalElectionResult> results) {
        return new ConfigWithLocalElections(config, LocalElectionType.KF, Set.of(), results);
    }

}
