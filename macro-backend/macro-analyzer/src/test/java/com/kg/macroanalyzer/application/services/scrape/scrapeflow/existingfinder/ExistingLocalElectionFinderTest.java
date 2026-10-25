package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExistingLocalElectionFinderTest {

    @Mock
    DatabasePort databasePort;

    @Test
    void shouldResolveTypeFromConfigName_andLoadStoredMd5s() {
        // given
        final var config = SeriesConfig.builder().name("RegionElections").build();

        // when
        when(databasePort.readLocalElectionSourceMd5s(LocalElectionType.RF)).thenReturn(Set.of("md5"));
        final var result = new ExistingLocalElectionFinder(databasePort).findExisting(config);

        // then
        assertTrue(result.isPresent());
        assertEquals(LocalElectionType.RF, result.get().type());
        assertEquals(Set.of("md5"), result.get().existingMd5s());
    }

    @Test
    void shouldReturnEmpty_whenConfigIsNotALocalElection() {
        // given
        final var config = SeriesConfig.builder().name("PolicyRateSweden").build();

        // when
        final var result = new ExistingLocalElectionFinder(databasePort).findExisting(config);

        // then
        assertTrue(result.isEmpty());
        verifyNoInteractions(databasePort);
    }

}
