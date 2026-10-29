package com.kg.macroanalyzer.election.application.service;

import com.kg.macroanalyzer.election.application.port.in.ElectedMemberDto;
import com.kg.macroanalyzer.election.application.port.in.GovernanceDto;
import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.election.domain.ElectedMember;
import com.kg.macroanalyzer.election.domain.Governance;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import com.kg.macroanalyzer.election.domain.LocalPartyResult;
import com.kg.macroanalyzer.parliament.application.port.in.PartyDirectory;
import com.kg.macroanalyzer.parliament.domain.Party;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalElectionServiceTest {

    @Mock
    ElectionRepositoryPort repositoryPort;
    @Mock
    PartyDirectory partyDirectory;

    LocalElectionService service;

    @BeforeEach
    void setUp() {
        service = new LocalElectionService(repositoryPort, partyDirectory);
    }

    @Test
    void shouldSummariseLatestYear_withLargestPartyByVotes() {
        // when
        when(repositoryPort.readLatestLocalElectionYear(LocalElectionType.KF)).thenReturn(Optional.of(2026));
        when(repositoryPort.readLocalElectionResults(LocalElectionType.KF, 2026)).thenReturn(List.of(lund()));
        final var summaries = service.getLocalElectionResults(LocalElectionType.KF, null);

        // then
        assertEquals(1, summaries.size());
        final var summary = summaries.getFirst();
        assertEquals("Lund", summary.name());
        assertEquals("S", summary.largestParty());
        assertEquals("#E8112D", summary.largestPartyColor());
        assertEquals(List.of("S", "M", "LL"), summary.parties().stream().map(p -> p.abbreviation()).toList());
        assertEquals("#9CA3AF", summary.parties().get(2).color());
    }

    @Test
    void shouldUseRiksdagPartyColour_overValmyndighetensColour() {
        // when
        when(partyDirectory.listParties()).thenReturn(List.of(
                new Party("M", "Moderaterna", "#52BDEC", "Liberal Conservatism", true)
        ));
        when(repositoryPort.readLocalElectionResults(LocalElectionType.KF, 2026)).thenReturn(List.of(lund()));
        final var summary = service.getLocalElectionResults(LocalElectionType.KF, 2026).getFirst();

        // then
        assertEquals("#52BDEC", summary.parties().get(1).color());
        assertEquals("#E8112D", summary.parties().get(0).color());
    }

    @Test
    void shouldReturnEmptyList_whenNothingScraped() {
        // when
        when(repositoryPort.readLatestLocalElectionYear(LocalElectionType.RF)).thenReturn(Optional.empty());

        // then
        assertEquals(List.of(), service.getLocalElectionResults(LocalElectionType.RF, null));
    }

    @Test
    void shouldUseRequestedYear_andGroupElectedMembersBySeats() {
        // when
        when(repositoryPort.readLocalElectionResult(LocalElectionType.KF, 2022, "1281")).thenReturn(Optional.of(lund()));
        when(repositoryPort.readGovernance(LocalElectionType.KF, 2022, "1281")).thenReturn(Optional.empty());
        final var detail = service.getLocalElectionResult(LocalElectionType.KF, 2022, "1281");

        // then
        assertTrue(detail.isPresent());
        assertNull(detail.get().governance());
        final var elected = detail.get().elected();
        assertEquals(List.of("S", "M"), elected.stream().map(p -> p.abbreviation()).toList());
        assertEquals(List.of(
                new ElectedMemberDto("Anna", 1, "Personröster"),
                new ElectedMemberDto("Bertil", 2, null)
        ), elected.getFirst().members());
        assertEquals("Socialdemokraterna", elected.getFirst().name());
    }

    @Test
    void shouldIncludeGovernance_whenCurated() {
        // given
        final var governance = new Governance(2026, LocalElectionType.KF, "1281", "Lundaalliansen", "M styr", List.of("M"), List.of());

        // when
        when(repositoryPort.readLatestLocalElectionYear(LocalElectionType.KF)).thenReturn(Optional.of(2026));
        when(repositoryPort.readLocalElectionResult(LocalElectionType.KF, 2026, "1281")).thenReturn(Optional.of(lund()));
        when(repositoryPort.readGovernance(LocalElectionType.KF, 2026, "1281")).thenReturn(Optional.of(governance));
        final var detail = service.getLocalElectionResult(LocalElectionType.KF, null, "1281");

        // then
        assertEquals(new GovernanceDto("Lundaalliansen", "M styr", List.of("M"), List.of()), detail.orElseThrow().governance());
        verify(repositoryPort).readGovernance(LocalElectionType.KF, 2026, "1281");
    }

    private static LocalElectionResult lund() {
        return LocalElectionResult.builder()
                .year(2026)
                .type(LocalElectionType.KF)
                .areaCode("1281")
                .areaName("Lund")
                .lanCode("12")
                .totalSeats(65)
                .parties(List.of(
                        new LocalPartyResult("0001", "M", "Moderaterna", "#66BEE6", 100, new BigDecimal("30.0"), 1),
                        new LocalPartyResult("0002", "S", "Socialdemokraterna", "#E8112D", 200, new BigDecimal("60.0"), 2),
                        new LocalPartyResult("1234", "LL", "Lundalistan", null, 30, new BigDecimal("10.0"), 0)
                ))
                .elected(List.of(
                        new ElectedMember("Bertil", "0002", "S", 2, null),
                        new ElectedMember("Cecilia", "0001", "M", 1, null),
                        new ElectedMember("Anna", "0002", "S", 1, "Personröster")
                ))
                .build();
    }

}
