package com.kg.macroanalyzer.parliament.application.service.scrape;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.domain.Election;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.kg.macroanalyzer.parliament.domain.TestMemberFactory.chamberSeat;
import static com.kg.macroanalyzer.parliament.domain.TestMemberFactory.member;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExistingMemberActivityFinderTest {

    ExistingMemberActivityFinder finder;

    @Mock
    ParliamentRepositoryPort repositoryPort;

    @BeforeEach
    void setUp() {
        finder = new ExistingMemberActivityFinder(repositoryPort, 2);
    }

    @Test
    void shouldReturnMembersOfLatestTerms_whenMembersStored() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var elections = List.of(
                election(2026, "2026-09-28", "2030-09-23"),
                election(2022, "2022-09-26", "2026-09-27"),
                election(2018, "2018-09-24", "2022-09-25")
        );
        final var members = List.of(
                member("new", "S", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2026-09-28", "2030-09-23"))),
                member("previous", "M", "Tidigare riksdagsledamot", List.of(chamberSeat("2022-09-26", "2026-09-28"))),
                member("old", "C", "Tidigare riksdagsledamot", List.of(chamberSeat("2018-09-24", "2022-09-26")))
        );

        // when
        when(repositoryPort.readMembersOfParliament()).thenReturn(members);
        when(repositoryPort.readElections()).thenReturn(elections);
        final var result = finder.findExisting(seriesConfig);

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of("new", "previous"), result.get().intressentIds());
    }

    @Test
    void shouldPostpone_whenNoMembersStoredYet() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        when(repositoryPort.readMembersOfParliament()).thenReturn(List.of());
        final var result = finder.findExisting(seriesConfig);

        // then
        assertTrue(result.isEmpty());
        verify(repositoryPort).readMembersOfParliament();
    }

    private static Election election(int year, String start, String end) {
        return new Election(year, LocalDate.parse(start), LocalDate.parse(end), Map.of());
    }

}
