package com.kg.macroanalyzer.parliament.application.service;

import com.kg.macroanalyzer.parliament.application.port.in.MemberSummaryDto;
import com.kg.macroanalyzer.parliament.application.port.in.VotesDto;
import com.kg.macroanalyzer.parliament.application.port.in.PartySeats;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.domain.Election;
import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.parliament.domain.Party;
import com.kg.macroanalyzer.parliament.domain.SessionVotes;
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
import java.util.Optional;

import static com.kg.macroanalyzer.parliament.domain.TestMemberFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParliamentServiceTest {

    private static final Clock TODAY = Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC);

    ParliamentService parliamentService;

    @Mock
    ParliamentRepositoryPort repositoryPort;

    @BeforeEach
    void setUp() {
        parliamentService = new ParliamentService(repositoryPort, TODAY);
        lenient().when(repositoryPort.readParties()).thenReturn(List.of(
                new Party("S", "Socialdemokraterna", "#E8112d", "Social Democracy", true),
                new Party("M", "Moderaterna", "#52BDEC", "Liberal Conservatism", true)
        ));
        lenient().when(repositoryPort.readElections()).thenReturn(List.of(
                election(2026, "2026-09-28", "2030-09-23", Map.of("S", 99, "M", 70)),
                election(2022, "2022-09-26", "2026-09-27", Map.of("S", 107, "M", 68))
        ));
        lenient().when(repositoryPort.readOrganNames()).thenReturn(Map.of("FiU", "Finansutskottet", "Fi", "Finansdepartementet"));
        lenient().when(repositoryPort.readMemberActivity()).thenReturn(List.of());
    }

    @Test
    void shouldListParties_asStored() {
        // when
        final var result = parliamentService.getParties();

        // then
        assertEquals(List.of("S", "M"), result.stream().map(p -> p.code()).toList());
        assertEquals("#52BDEC", result.get(1).color());
    }

    @Test
    void shouldReturnElections_withPartyNamesAndSeats() {
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
    void shouldOrderElectionParties_leftToRight_withOtherPartiesLastBySeats() {
        // given
        when(repositoryPort.readElections()).thenReturn(List.of(election(2026, "2026-09-28", "2030-09-23", Map.of(
                "S", 99, "M", 70, "SD", 62, "V", 30, "C", 25, "KD", 22, "MP", 22, "L", 19, "NYD", 5, "X", 7
        ))));

        // when
        final var result = parliamentService.getElections().getFirst().parties();

        // then
        assertEquals(List.of("V", "S", "MP", "C", "L", "M", "KD", "SD", "X", "NYD"), result.stream().map(PartySeats::code).toList());
    }

    @Test
    void shouldOrderCompositionParties_leftToRight_withEquallySizedOthersByCode() {
        // given
        when(repositoryPort.countMembersByPartyWhereStatusContainsIgnoringCase("tjänstgörande")).thenReturn(Map.of("SD", 62, "S", 99, "V", 30, "NYD", 3, "-", 3));

        // when
        final var result = parliamentService.getComposition().parties();

        // then
        assertEquals(List.of("V", "S", "SD", "-", "NYD"), result.stream().map(PartySeats::code).toList());
    }

    @Test
    void shouldReturnMembersOfSelectedTerm_sortedByName() {
        // given
        when(repositoryPort.readMembersOfParliament()).thenReturn(List.of(
                member("B", "S", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2026-09-28", "2030-09-23"))),
                member("A", "M", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2022-09-26", "2030-09-23"))),
                member("C", "S", "Tidigare riksdagsledamot", List.of(chamberSeat("2022-09-26", "2026-09-28")))
        ));

        // when
        final var latest = parliamentService.getMembersOfParliament(null);
        final var previous = parliamentService.getMembersOfParliament(2022);

        // then
        assertEquals(List.of("A", "B"), latest.stream().map(MemberSummaryDto::id).toList());
        assertEquals(List.of("A", "C"), previous.stream().map(MemberSummaryDto::id).toList());
    }

    @Test
    void shouldIncludeActivityInMemberList_withAndWithoutActivityRow() {
        // given
        when(repositoryPort.readMembersOfParliament()).thenReturn(List.of(
                member("A", "S", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2026-09-28", "2030-09-23"))),
                member("B", "M", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2026-09-28", "2030-09-23")))
        ));
        when(repositoryPort.readMemberActivity()).thenReturn(List.of(MemberActivity.builder()
                .intressentId("A").votesYes(90).votesNo(5).votesAbsent(4).votesAbstain(1).speechCount(12).motionCount(3)
                .votesBySession(List.of()).recentSpeeches(List.of()).recentMotions(List.of())
                .build()));

        // when
        final var result = parliamentService.getMembersOfParliament(null);

        // then
        final var withActivity = result.getFirst();
        assertEquals(new VotesDto(90, 5, 4, 1), withActivity.votes());
        assertEquals(12, withActivity.speechCount());
        assertEquals(3, withActivity.motionCount());
        final var withoutActivity = result.getLast();
        assertNull(withoutActivity.attendancePct());
        assertNull(withoutActivity.votes());
        assertNull(withoutActivity.speechCount());
        assertNull(withoutActivity.motionCount());
    }

    @Test
    void shouldReturnNoMembers_whenElectionYearUnknown() {
        // when
        final var result = parliamentService.getMembersOfParliament(1850);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCountMembersWithAServingStatus_forComposition() {
        // given
        when(repositoryPort.countMembersByPartyWhereStatusContainsIgnoringCase("tjänstgörande")).thenReturn(Map.of("S", 2, "M", 1));

        // when
        final var result = parliamentService.getComposition();

        // then
        assertEquals("2026-10-25", result.asOf());
        assertEquals(List.of(2, 1), result.parties().stream().map(PartySeats::seats).toList());
    }

    @Test
    void shouldNameMembersWithoutAParty_forComposition() {
        // given
        when(repositoryPort.readParties()).thenReturn(List.of(new Party("-", "Politisk vilde", "#9CA3AF", null, false)));
        when(repositoryPort.countMembersByPartyWhereStatusContainsIgnoringCase("tjänstgörande")).thenReturn(Map.of("-", 2, "", 1));

        // when
        final var result = parliamentService.getComposition();

        // then
        assertEquals(List.of("Politisk vilde", "Okänt parti"), result.parties().stream().map(PartySeats::name).toList());
    }

    @Test
    void shouldPreferMinisterRole_overCommitteeChair() {
        // given
        when(repositoryPort.readMembersOfParliament()).thenReturn(List.of(member("1", "M", "Tjänstledig riksdagsledamot", List.of(
                chamberSeat("2026-09-28", "2030-09-23"),
                uppdrag("uppdrag", "FiU", "Ordförande", "", "2026-10-06", "2030-09-23"),
                uppdrag("Departement", "Fi", "Finansminister", "", "2026-10-01", "")
        ))));

        // when
        final var result = parliamentService.getMembersOfParliament(2026).getFirst();

        // then
        assertEquals("Finansminister", result.currentRole());
    }

    @Test
    void shouldFallBackToCommitteeSeat_thenToStatus() {
        // given
        when(repositoryPort.readMembersOfParliament()).thenReturn(List.of(
                member("1", "S", "Tjänstgörande riksdagsledamot", List.of(
                        chamberSeat("2026-09-28", "2030-09-23"),
                        uppdrag("uppdrag", "FiU", "Ledamot", "", "2026-10-06", "2030-09-23")
                )),
                member("2", "S", "Tjänstgörande riksdagsledamot", List.of(chamberSeat("2026-09-28", "2030-09-23")))
        ));

        // when
        final var result = parliamentService.getMembersOfParliament(2026);

        // then
        assertEquals("Ledamot, Finansutskottet", result.get(0).currentRole());
        assertEquals("Tjänstgörande riksdagsledamot", result.get(1).currentRole());
    }

    @Test
    void shouldMergeOverlappingSeats_whenCountingYearsInParliament() {
        // given
        when(repositoryPort.readMembersOfParliament()).thenReturn(List.of(member("1", "S", "Tjänstgörande riksdagsledamot", List.of(
                chamberSeat("2018-09-24", "2022-09-26"),
                chamberSeat("2022-09-26", "2026-09-28"),
                chamberSeat("2022-09-26", "2024-01-01"),
                chamberSeat("2026-09-28", "2030-09-23")
        ))));

        // when
        final var result = parliamentService.getMembersOfParliament(2026).getFirst();

        // then
        assertEquals(8.1, result.yearsInParliament());
    }

    @Test
    void shouldBuildDetail_withContactAssignmentsAndActivity() {
        // given
        when(repositoryPort.readMemberOfParliament("1")).thenReturn(Optional.of(member(
                "1", "S", "Tjänstgörande riksdagsledamot",
                List.of(chamberSeat("2026-09-28", "2030-09-23"),
                        uppdrag("uppdrag", "FiU", "Suppleant", "", "2026-10-06", "2030-09-23")),
                List.of(uppgift("Officiell e-postadress", "eadress", "anna.andersson[på]riksdagen.se"),
                        uppgift("Utbildning", "biografi", "Juristexamen"),
                        uppgift("KandiderarINastaVal", "val", "true"))
        )));
        when(repositoryPort.readMemberActivity()).thenReturn(List.of(MemberActivity.builder()
                .intressentId("1").votesYes(90).votesNo(5).votesAbsent(5).speechCount(12).motionCount(3)
                .votesBySession(List.of(new SessionVotes("2026/27", 90, 5, 5, 0)))
                .recentSpeeches(List.of()).recentMotions(List.of())
                .build()));

        // when
        final var result = parliamentService.getMemberOfParliament("1").orElseThrow();

        // then
        assertEquals("anna.andersson@riksdagen.se", result.contact().email());
        assertEquals(Boolean.TRUE, result.runningNextElection());
        assertEquals("Juristexamen", result.biography().getFirst().text());
        assertEquals("Finansutskottet", result.assignments().getFirst().organName());
        assertTrue(result.assignments().getFirst().current());
        assertEquals(95.0, result.summary().attendancePct());
        assertEquals(12, result.speechCount());
        assertEquals(List.of(new SessionVotes("2026/27", 90, 5, 5, 0)), result.votesBySession());
        assertEquals(1980, result.birthYear());
    }

    @Test
    void shouldReturnEmpty_whenMemberUnknown() {
        // given
        when(repositoryPort.readMemberOfParliament("missing")).thenReturn(Optional.empty());

        // when / then
        assertTrue(parliamentService.getMemberOfParliament("missing").isEmpty());
    }

    private static Election election(int year, String start, String end, Map<String, Integer> seats) {
        return new Election(year, LocalDate.parse(start), LocalDate.parse(end), new LinkedHashMap<>(seats));
    }

}
