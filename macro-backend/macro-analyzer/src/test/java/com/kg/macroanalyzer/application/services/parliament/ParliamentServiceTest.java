package com.kg.macroanalyzer.application.services.parliament;

import com.kg.macroanalyzer.application.domain.parliament.Election;
import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.domain.parliament.Party;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.MemberSummaryDto;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.PartySeats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.kg.macroanalyzer.TestMemberFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParliamentServiceTest {

    private static final Clock TODAY = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC);

    ParliamentService parliamentService;

    @Mock
    DatabasePort databasePort;

    @BeforeEach
    void setUp() {
        parliamentService = new ParliamentService(databasePort, TODAY);
        lenient().when(databasePort.readParties()).thenReturn(List.of(
                new Party("S", "Socialdemokraterna", "#E8112d", "Social Democracy", true),
                new Party("M", "Moderaterna", "#52BDEC", "Liberal Conservatism", true)
        ));
        lenient().when(databasePort.readElections()).thenReturn(List.of(
                election(2026, "2026-09-28", "2030-09-23", Map.of("S", 99, "M", 70)),
                election(2022, "2022-09-26", "2026-09-27", Map.of("S", 107, "M", 68))
        ));
        lenient().when(databasePort.readOrganNames()).thenReturn(Map.of("FiU", "Finansutskottet", "Fi", "Finansdepartementet"));
        lenient().when(databasePort.readMemberActivity()).thenReturn(List.of());
    }

    @Test
    void shouldReturnElections_withPartyNamesAndSeatsDescending() {
        // when
        final var result = parliamentService.getElections();

        // then
        assertEquals(2026, result.getFirst().year());
        assertEquals(List.of(
                new PartySeats("S", "Socialdemokraterna", "#E8112d", 99),
                new PartySeats("M", "Moderaterna", "#52BDEC", 70)
        ), result.getFirst().parties());
    }

    @Test
    void shouldReturnMembersOfSelectedTerm_sortedByName() {
        // given
        when(databasePort.readMembersOfParliament()).thenReturn(List.of(
                member("B", "S", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2026-09-28", "2030-09-23"))),
                member("A", "M", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2022-09-26", "2030-09-23"))),
                member("C", "S", "Tidigare riksdagsledamot", List.of(chamberSeat("2022-09-26", "2026-09-28")))
        ));

        // when
        final var latest = parliamentService.getMembers(null);
        final var previous = parliamentService.getMembers(2022);

        // then
        assertEquals(List.of("A", "B"), latest.stream().map(MemberSummaryDto::id).toList());
        assertEquals(List.of("A", "C"), previous.stream().map(MemberSummaryDto::id).toList());
    }

    @Test
    void shouldReturnNoMembers_whenElectionYearUnknown() {
        // when
        final var result = parliamentService.getMembers(1850);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCountServingMembersPerParty_forComposition() {
        // given
        when(databasePort.readMembersOfParliament()).thenReturn(List.of(
                member("1", "S", "Tjänstgörande riksdagsledamot", List.of()),
                member("2", "S", "Tjänstgörande statsrådsersättare", List.of()),
                member("3", "M", "Tjänstledig riksdagsledamot", List.of()),
                member("4", "M", "Tjänstgörande riksdagsledamot", List.of())
        ));

        // when
        final var result = parliamentService.getComposition();

        // then
        assertEquals("2026-10-25", result.asOf());
        assertEquals(List.of(2, 1), result.parties().stream().map(PartySeats::seats).toList());
    }

    @Test
    void shouldPreferMinisterRole_overCommitteeChair() {
        // given
        when(databasePort.readMembersOfParliament()).thenReturn(List.of(member("1", "M", "Tjänstledig riksdagsledamot", List.of(
                chamberSeat("2026-09-28", "2030-09-23"),
                uppdrag("uppdrag", "FiU", "Ordförande", "", "2026-10-06", "2030-09-23"),
                uppdrag("Departement", "Fi", "Finansminister", "", "2026-10-01", "")
        ))));

        // when
        final var result = parliamentService.getMembers(2026).getFirst();

        // then
        assertEquals("Finansminister", result.currentRole());
    }

    @Test
    void shouldFallBackToCommitteeSeat_thenToStatus() {
        // given
        when(databasePort.readMembersOfParliament()).thenReturn(List.of(
                member("1", "S", "Tjänstgörande riksdagsledamot", List.of(
                        chamberSeat("2026-09-28", "2030-09-23"),
                        uppdrag("uppdrag", "FiU", "Ledamot", "", "2026-10-06", "2030-09-23")
                )),
                member("2", "S", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2026-09-28", "2030-09-23")))
        ));

        // when
        final var result = parliamentService.getMembers(2026);

        // then
        assertEquals("Ledamot, Finansutskottet", result.get(0).currentRole());
        assertEquals("Tjänstgörande riksdagsledamot", result.get(1).currentRole());
    }

    @Test
    void shouldMergeOverlappingSeats_whenCountingYearsInParliament() {
        // given
        when(databasePort.readMembersOfParliament()).thenReturn(List.of(member("1", "S", "Tjänstgörande riksdagsledamot", List.of(
                chamberSeat("2018-09-24", "2022-09-26"),
                chamberSeat("2022-09-26", "2026-09-28"),
                chamberSeat("2022-09-26", "2024-01-01"),
                chamberSeat("2026-09-28", "2030-09-23")
        ))));

        // when
        final var result = parliamentService.getMembers(2026).getFirst();

        // then
        assertEquals(8.1, result.yearsInParliament());
    }

    @Test
    void shouldBuildDetail_withContactAssignmentsAndActivity() {
        // given
        when(databasePort.readMembersOfParliament()).thenReturn(List.of(member(
                "1", "S", "Tjänstgörande riksdagsledamot",
                List.of(chamberSeat("2026-09-28", "2030-09-23"),
                        uppdrag("uppdrag", "FiU", "Suppleant", "", "2026-10-06", "2030-09-23")),
                List.of(uppgift("Officiell e-postadress", "eadress", "anna.andersson[på]riksdagen.se"),
                        uppgift("Utbildning", "biografi", "Juristexamen"),
                        uppgift("KandiderarINastaVal", "val", "true"))
        )));
        when(databasePort.readMemberActivity()).thenReturn(List.of(MemberActivity.builder()
                .intressentId("1").votesYes(90).votesNo(5).votesAbsent(5).speechCount(12).motionCount(3)
                .recentSpeeches(List.of()).recentMotions(List.of())
                .build()));

        // when
        final var result = parliamentService.getMember("1").orElseThrow();

        // then
        assertEquals("anna.andersson@riksdagen.se", result.contact().email());
        assertEquals(Boolean.TRUE, result.runningNextElection());
        assertEquals("Juristexamen", result.biography().getFirst().text());
        assertEquals("Finansutskottet", result.assignments().getFirst().organName());
        assertTrue(result.assignments().getFirst().current());
        assertEquals(95.0, result.summary().attendancePct());
        assertEquals(12, result.speechCount());
        assertEquals(1980, result.birthYear());
    }

    @Test
    void shouldReturnEmpty_whenMemberUnknown() {
        // given
        when(databasePort.readMembersOfParliament()).thenReturn(List.of());

        // when / then
        assertTrue(parliamentService.getMember("missing").isEmpty());
    }

    private static Election election(int year, String start, String end, Map<String, Integer> seats) {
        return new Election(year, LocalDate.parse(start), LocalDate.parse(end), new LinkedHashMap<>(seats));
    }

}
