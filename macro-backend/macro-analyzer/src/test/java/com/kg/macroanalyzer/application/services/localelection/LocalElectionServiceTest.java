package com.kg.macroanalyzer.application.services.localelection;

import com.kg.macroanalyzer.application.domain.localelection.ElectedMember;
import com.kg.macroanalyzer.application.domain.localelection.Governance;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.localelection.LocalPartyResult;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.ElectedMemberDto;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.GovernanceDto;
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
    DatabasePort databasePort;

    LocalElectionService service;

    @BeforeEach
    void setUp() {
        service = new LocalElectionService(databasePort);
    }

    @Test
    void shouldSummariseLatestYear_withLargestPartyByVotes() {
        // when
        when(databasePort.readLatestLocalElectionYear(LocalElectionType.KF)).thenReturn(Optional.of(2026));
        when(databasePort.readLocalElectionResults(LocalElectionType.KF, 2026)).thenReturn(List.of(lund()));
        final var summaries = service.getResults(LocalElectionType.KF, null);

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
    void shouldReturnEmptyList_whenNothingScraped() {
        // when
        when(databasePort.readLatestLocalElectionYear(LocalElectionType.RF)).thenReturn(Optional.empty());

        // then
        assertEquals(List.of(), service.getResults(LocalElectionType.RF, null));
    }

    @Test
    void shouldUseRequestedYear_andGroupElectedMembersBySeats() {
        // when
        when(databasePort.readLocalElectionResult(LocalElectionType.KF, 2022, "1281")).thenReturn(Optional.of(lund()));
        when(databasePort.readGovernance(LocalElectionType.KF, 2022, "1281")).thenReturn(Optional.empty());
        final var detail = service.getResult(LocalElectionType.KF, 2022, "1281");

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
        when(databasePort.readLatestLocalElectionYear(LocalElectionType.KF)).thenReturn(Optional.of(2026));
        when(databasePort.readLocalElectionResult(LocalElectionType.KF, 2026, "1281")).thenReturn(Optional.of(lund()));
        when(databasePort.readGovernance(LocalElectionType.KF, 2026, "1281")).thenReturn(Optional.of(governance));
        final var detail = service.getResult(LocalElectionType.KF, null, "1281");

        // then
        assertEquals(new GovernanceDto("Lundaalliansen", "M styr", List.of("M"), List.of()), detail.orElseThrow().governance());
        verify(databasePort).readGovernance(LocalElectionType.KF, 2026, "1281");
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
