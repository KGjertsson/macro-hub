package com.kg.macroanalyzer.application.services.parliament;

import com.kg.macroanalyzer.application.domain.parliament.*;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ParliamentService {

    private static final String SERVING_STATUS_PREFIX = "Tjänstgörande";
    private static final String SERVING_ASSIGNMENT_STATUS = "Tjänstgörande";
    private static final String UNKNOWN_PARTY_COLOR = "#9CA3AF";
    private static final String COMMITTEE_MEMBER_ROLE = "Ledamot";
    private static final Set<String> CHAIR_ROLES = Set.of(
            "Ordförande", "Vice ordförande", "Förste vice ordförande", "Andre vice ordförande", "Tredje vice ordförande"
    );

    private final DatabasePort databasePort;
    private final Clock clock;

    @Autowired
    public ParliamentService(DatabasePort databasePort) {
        this(databasePort, Clock.systemDefaultZone());
    }

    ParliamentService(DatabasePort databasePort, Clock clock) {
        this.databasePort = databasePort;
        this.clock = clock;
    }

    public List<PartyDto> getParties() {
        return databasePort.readParties().stream()
                .map(p -> PartyDto.builder()
                        .code(p.code())
                        .name(p.name())
                        .color(p.color())
                        .ideology(p.ideology())
                        .active(p.active())
                        .build())
                .toList();
    }

    public List<ElectionResultDto> getElections() {
        final var parties = partiesByCode();

        return databasePort.readElections().stream()
                .map(e -> ElectionResultDto.builder()
                        .year(e.year())
                        .termStart(e.termStart().toString())
                        .termEnd(e.termEnd().toString())
                        .parties(toPartySeats(e.seatsByParty(), parties))
                        .build())
                .toList();
    }

    public CompositionDto getComposition() {
        final var seats = databasePort.readMembersOfParliament().stream()
                .filter(m -> m.status() != null && m.status().startsWith(SERVING_STATUS_PREFIX))
                .collect(Collectors.groupingBy(m -> Objects.requireNonNullElse(m.parti(), ""), Collectors.summingInt(m -> 1)));

        return CompositionDto.builder()
                .asOf(today().toString())
                .parties(toPartySeats(seats, partiesByCode()))
                .build();
    }

    public List<MemberSummaryDto> getMembers(Integer year) {
        final var elections = databasePort.readElections();
        final var election = elections.stream()
                .filter(e -> year == null || e.year() == year)
                .findFirst();
        if (election.isEmpty()) {
            return List.of();
        }
        final var term = election.get();
        final var context = buildContext();

        return databasePort.readMembersOfParliament().stream()
                .filter(m -> m.satInChamberDuring(term.termStart(), term.termEnd()))
                .sorted(Comparator.comparing(m -> Objects.requireNonNullElse(m.sorteringsnamn(), "")))
                .map(m -> toSummary(m, context))
                .toList();
    }

    public Optional<MemberDetailDto> getMember(String intressentId) {
        final var context = buildContext();

        return databasePort.readMembersOfParliament().stream()
                .filter(m -> intressentId.equals(m.intressent_id()))
                .findFirst()
                .map(m -> toDetail(m, context));
    }

    private MemberDetailDto toDetail(MemberOfParliament member, Context context) {
        final var activity = Optional.ofNullable(context.activity().get(member.intressent_id()));

        return MemberDetailDto.builder()
                .summary(toSummary(member, context))
                .imageUrlLarge(member.bild_url_max())
                .birthYear(parseInt(member.fodd_ar()))
                .gender(member.kon())
                .residence(blankToNull(member.iort()))
                .runningNextElection(detail(member, "KandiderarINastaVal").map(Boolean::parseBoolean).orElse(null))
                .contact(ContactDto.builder()
                        .email(detail(member, "Officiell e-postadress").map(e -> e.replace("[på]", "@")).orElse(null))
                        .phone(detail(member, "Tjänstetelefon").orElse(null))
                        .website(detail(member, "Webbsida").orElse(null))
                        .build())
                .assignments(toAssignments(member, context))
                .biography(member.details().stream()
                        .filter(u -> "biografi".equals(u.typ()))
                        .map(u -> new BiographyEntryDto(u.kod(), textOf(u)))
                        .filter(b -> !b.text().isBlank())
                        .toList())
                .votes(activity.map(a -> new VotesDto(a.votesYes(), a.votesNo(), a.votesAbsent(), a.votesAbstain()))
                        .orElse(null))
                .votesBySession(activity.map(MemberActivity::votesBySession).orElse(List.of()))
                .speechCount(activity.map(MemberActivity::speechCount).orElse(null))
                .motionCount(activity.map(MemberActivity::motionCount).orElse(null))
                .recentSpeeches(activity.map(MemberActivity::recentSpeeches).orElse(List.of()))
                .recentMotions(activity.map(MemberActivity::recentMotions).orElse(List.of()))
                .build();
    }

    private MemberSummaryDto toSummary(MemberOfParliament member, Context context) {
        final var activity = context.activity().get(member.intressent_id());

        return MemberSummaryDto.builder()
                .id(member.intressent_id())
                .name(member.fullName())
                .party(member.parti())
                .constituency(member.valkrets())
                .status(member.status())
                .imageUrl(member.bild_url_192())
                .currentRole(currentRole(member, context))
                .yearsInParliament(yearsInParliament(member))
                .attendancePct(activity == null ? null : activity.attendancePct())
                .build();
    }

    private List<AssignmentDto> toAssignments(MemberOfParliament member, Context context) {
        return member.assignments().stream()
                .sorted(Comparator.comparing(Uppdrag::fromDate).reversed())
                .map(u -> AssignmentDto.builder()
                        .type(u.typ())
                        .organCode(u.organ_kod())
                        .organName(organName(u, context))
                        .role(u.roll_kod())
                        .status(blankToNull(u.status()))
                        .from(u.fromDate().equals(LocalDate.MIN) ? null : u.fromDate().toString())
                        .to(u.tomDate().equals(LocalDate.MAX) ? null : u.tomDate().toString())
                        .current(u.activeOn(today()))
                        .build())
                .toList();
    }

    // Most prominent current assignment: minister, speaker, committee chair, party role, committee seat, otherwise the status
    private String currentRole(MemberOfParliament member, Context context) {
        final var active = member.assignments().stream()
                .filter(u -> u.activeOn(today()))
                .toList();

        return firstOf(active, "Departement", u -> true, u -> "Statsråd".equals(u.roll_kod())
                        ? "Statsråd, " + organName(u, context)
                        : u.roll_kod())
                .or(() -> firstOf(active, "talmansuppdrag", u -> true, Uppdrag::roll_kod))
                .or(() -> firstOf(active, "uppdrag", u -> CHAIR_ROLES.contains(u.roll_kod()),
                        u -> "%s, %s".formatted(u.roll_kod(), organName(u, context))))
                .or(() -> firstOf(active, "partiuppdrag", u -> true,
                        u -> "%s (%s)".formatted(u.roll_kod(), u.organ_kod())))
                .or(() -> firstOf(active, "uppdrag", u -> COMMITTEE_MEMBER_ROLE.equals(u.roll_kod()),
                        u -> "%s, %s".formatted(u.roll_kod(), organName(u, context))))
                .orElse(member.status());
    }

    private static Optional<String> firstOf(
            List<Uppdrag> assignments,
            String type,
            Predicate<Uppdrag> filter,
            Function<Uppdrag, String> label
    ) {
        return assignments.stream()
                .filter(u -> type.equals(u.typ()))
                .filter(filter)
                .findFirst()
                .map(label);
    }

    // Serving periods can overlap (e.g. re-election day), so intervals are merged before summing
    private double yearsInParliament(MemberOfParliament member) {
        final var today = today();
        final var intervals = member.chamberAssignments().stream()
                .filter(u -> SERVING_ASSIGNMENT_STATUS.equals(u.status()))
                .filter(u -> u.fromDate().isBefore(today))
                .map(u -> new LocalDate[]{u.fromDate(), u.tomDate().isAfter(today) ? today : u.tomDate()})
                .sorted(Comparator.comparing(i -> i[0]))
                .toList();
        long days = 0;
        LocalDate currentStart = null;
        LocalDate currentEnd = null;
        for (final var interval : intervals) {
            if (currentEnd == null || interval[0].isAfter(currentEnd)) {
                if (currentEnd != null) {
                    days += ChronoUnit.DAYS.between(currentStart, currentEnd);
                }
                currentStart = interval[0];
                currentEnd = interval[1];
            } else if (interval[1].isAfter(currentEnd)) {
                currentEnd = interval[1];
            }
        }
        if (currentEnd != null) {
            days += ChronoUnit.DAYS.between(currentStart, currentEnd);
        }

        return Math.round(days / 365.25 * 10) / 10.0;
    }

    private String organName(Uppdrag uppdrag, Context context) {
        final var code = uppdrag.organ_kod();
        if ("partiuppdrag".equals(uppdrag.typ())) {
            return Optional.ofNullable(context.parties().get(code)).map(Party::name).orElse(code);
        }

        return context.organNames().getOrDefault(code, code);
    }

    private static List<PartySeats> toPartySeats(Map<String, Integer> seatsByParty, Map<String, Party> parties) {
        return seatsByParty.entrySet().stream()
                .map(e -> {
                    final var party = parties.get(e.getKey());
                    return PartySeats.builder()
                            .code(e.getKey())
                            .name(party == null ? partyFallbackName(e.getKey()) : party.name())
                            .color(party == null ? UNKNOWN_PARTY_COLOR : party.color())
                            .seats(e.getValue())
                            .build();
                })
                .sorted(Comparator.comparing(PartySeats::seats).reversed())
                .toList();
    }

    private static String partyFallbackName(String code) {
        return code == null || code.isBlank() ? "Okänt parti" : code;
    }

    private static Optional<String> detail(MemberOfParliament member, String code) {
        return member.details().stream()
                .filter(u -> code.equals(u.kod()))
                .map(ParliamentService::textOf)
                .filter(t -> !t.isBlank())
                .findFirst();
    }

    private static String textOf(Uppgift uppgift) {
        return Optional.ofNullable(uppgift.uppgift()).orElse(List.of()).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.joining("\n"))
                .trim();
    }

    private static Integer parseInt(String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Map<String, Party> partiesByCode() {
        return databasePort.readParties().stream()
                .collect(Collectors.toMap(Party::code, Function.identity()));
    }

    private Context buildContext() {
        final var activity = databasePort.readMemberActivity().stream()
                .collect(Collectors.toMap(MemberActivity::intressentId, Function.identity(), (a, b) -> a));

        return new Context(partiesByCode(), databasePort.readOrganNames(), activity);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private record Context(
            Map<String, Party> parties,
            Map<String, String> organNames,
            Map<String, MemberActivity> activity
    ) {
    }

}
