package com.kg.macroanalyzer.application.services.localelection;

import com.kg.macroanalyzer.application.domain.localelection.ElectedMember;
import com.kg.macroanalyzer.application.domain.localelection.Governance;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.localelection.LocalPartyResult;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LocalElectionService {

    private static final String UNKNOWN_PARTY_COLOR = "#9CA3AF";

    private final DatabasePort databasePort;

    @Autowired
    public LocalElectionService(DatabasePort databasePort) {
        this.databasePort = databasePort;
    }

    public List<LocalElectionSummaryDto> getResults(LocalElectionType type, Integer year) {
        return resolveYear(type, year)
                .map(y -> databasePort.readLocalElectionResults(type, y).stream()
                        .map(LocalElectionService::toSummary)
                        .toList())
                .orElse(List.of());
    }

    public Optional<LocalElectionDetailDto> getResult(LocalElectionType type, Integer year, String areaCode) {
        return resolveYear(type, year)
                .flatMap(y -> databasePort.readLocalElectionResult(type, y, areaCode)
                        .map(result -> LocalElectionDetailDto.builder()
                                .summary(toSummary(result))
                                .elected(electedByParty(result))
                                .governance(databasePort.readGovernance(type, y, areaCode)
                                        .map(LocalElectionService::toGovernance)
                                        .orElse(null))
                                .build()));
    }

    private Optional<Integer> resolveYear(LocalElectionType type, Integer year) {
        return year != null
                ? Optional.of(year)
                : databasePort.readLatestLocalElectionYear(type);
    }

    private static LocalElectionSummaryDto toSummary(LocalElectionResult result) {
        final var parties = result.parties().stream()
                .sorted(Comparator.comparingInt(LocalPartyResult::votes).reversed())
                .toList();
        final var largest = parties.stream().findFirst();

        return LocalElectionSummaryDto.builder()
                .year(result.year())
                .code(result.areaCode())
                .name(result.areaName())
                .lanCode(result.lanCode())
                .totalSeats(result.totalSeats())
                .turnout(result.turnout())
                .largestParty(largest.map(LocalPartyResult::abbreviation).orElse(null))
                .largestPartyColor(largest.map(LocalElectionService::colorOf).orElse(UNKNOWN_PARTY_COLOR))
                .parties(parties.stream()
                        .map(p -> LocalPartyResultDto.builder()
                                .code(p.code())
                                .abbreviation(p.abbreviation())
                                .name(p.name())
                                .color(colorOf(p))
                                .votes(p.votes())
                                .voteShare(p.voteShare())
                                .seats(p.seats())
                                .build())
                        .toList())
                .build();
    }

    // Parties are ordered by seats so the largest groups come first
    private static List<ElectedPartyDto> electedByParty(LocalElectionResult result) {
        final var partiesByCode = result.parties().stream()
                .collect(Collectors.toMap(LocalPartyResult::code, Function.identity(), (a, b) -> a));
        final var membersByParty = result.elected().stream()
                .sorted(Comparator.comparingInt(ElectedMember::order))
                .collect(Collectors.groupingBy(ElectedMember::partyCode, LinkedHashMap::new, Collectors.toList()));

        return membersByParty.entrySet().stream()
                .map(e -> {
                    final var party = Optional.ofNullable(partiesByCode.get(e.getKey()));
                    final var first = e.getValue().getFirst();

                    return ElectedPartyDto.builder()
                            .partyCode(e.getKey())
                            .abbreviation(first.partyAbbreviation())
                            .name(party.map(LocalPartyResult::name).orElse(first.partyAbbreviation()))
                            .color(party.map(LocalElectionService::colorOf).orElse(UNKNOWN_PARTY_COLOR))
                            .members(e.getValue().stream()
                                    .map(m -> ElectedMemberDto.builder()
                                            .name(m.name())
                                            .order(m.order())
                                            .electedOn(m.electedOn())
                                            .build())
                                    .toList())
                            .build();
                })
                .sorted(Comparator.comparingInt((ElectedPartyDto p) -> p.members().size()).reversed())
                .toList();
    }

    private static GovernanceDto toGovernance(Governance governance) {
        return GovernanceDto.builder()
                .coalitionName(governance.coalitionName())
                .description(governance.description())
                .governingParties(governance.governingParties())
                .supportParties(governance.supportParties())
                .build();
    }

    private static String colorOf(LocalPartyResult party) {
        return party.color() == null || party.color().isBlank() ? UNKNOWN_PARTY_COLOR : party.color();
    }

}
