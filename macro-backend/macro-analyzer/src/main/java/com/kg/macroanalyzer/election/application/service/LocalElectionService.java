package com.kg.macroanalyzer.election.application.service;

import com.kg.macroanalyzer.election.application.port.in.ElectedMemberDto;
import com.kg.macroanalyzer.election.application.port.in.ElectedPartyDto;
import com.kg.macroanalyzer.election.application.port.in.ElectionQueries;
import com.kg.macroanalyzer.election.application.port.in.GovernanceDto;
import com.kg.macroanalyzer.election.application.port.in.LocalElectionDetailDto;
import com.kg.macroanalyzer.election.application.port.in.LocalElectionSummaryDto;
import com.kg.macroanalyzer.election.application.port.in.LocalPartyResultDto;
import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.election.domain.ElectedMember;
import com.kg.macroanalyzer.election.domain.Governance;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import com.kg.macroanalyzer.election.domain.LocalPartyResult;
import com.kg.macroanalyzer.parliament.application.port.in.PartyDirectory;
import com.kg.macroanalyzer.parliament.domain.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LocalElectionService implements ElectionQueries {

    private static final String UNKNOWN_PARTY_COLOR = "#9CA3AF";

    private final ElectionRepositoryPort repositoryPort;
    private final PartyDirectory partyDirectory;

    @Autowired
    public LocalElectionService(ElectionRepositoryPort repositoryPort, PartyDirectory partyDirectory) {
        this.repositoryPort = repositoryPort;
        this.partyDirectory = partyDirectory;
    }

    @Override
    public List<LocalElectionSummaryDto> getLocalElectionResults(LocalElectionType type, Integer year) {
        return resolveYear(type, year)
                .map(y -> {
                    final var colors = partyColors();
                    return repositoryPort.readLocalElectionResults(type, y).stream()
                            .map(result -> toSummary(result, colors))
                            .toList();
                })
                .orElse(List.of());
    }

    @Override
    public Optional<LocalElectionDetailDto> getLocalElectionResult(LocalElectionType type, Integer year, String areaCode) {
        return resolveYear(type, year)
                .flatMap(y -> repositoryPort.readLocalElectionResult(type, y, areaCode)
                        .map(result -> {
                            final var colors = partyColors();
                            return LocalElectionDetailDto.builder()
                                    .summary(toSummary(result, colors))
                                    .elected(electedByParty(result, colors))
                                    .governance(repositoryPort.readGovernance(type, y, areaCode)
                                            .map(LocalElectionService::toGovernance)
                                            .orElse(null))
                                    .build();
                        }));
    }

    private Optional<Integer> resolveYear(LocalElectionType type, Integer year) {
        return year != null
                ? Optional.of(year)
                : repositoryPort.readLatestLocalElectionYear(type);
    }

    // Riksdag parties use the app's own palette so a party has the same colour on every page;
    // Valmyndigheten's own colours (e.g. blue for SD) are only kept for local parties
    private Map<String, String> partyColors() {
        return partyDirectory.listParties().stream()
                .collect(Collectors.toMap(Party::code, Party::color, (a, b) -> a));
    }

    private static LocalElectionSummaryDto toSummary(LocalElectionResult result, Map<String, String> colors) {
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
                .largestPartyColor(largest.map(p -> colorOf(p, colors)).orElse(UNKNOWN_PARTY_COLOR))
                .parties(parties.stream()
                        .map(p -> LocalPartyResultDto.builder()
                                .code(p.code())
                                .abbreviation(p.abbreviation())
                                .name(p.name())
                                .color(colorOf(p, colors))
                                .votes(p.votes())
                                .voteShare(p.voteShare())
                                .seats(p.seats())
                                .build())
                        .toList())
                .build();
    }

    // Parties are ordered by seats so the largest groups come first
    private static List<ElectedPartyDto> electedByParty(LocalElectionResult result, Map<String, String> colors) {
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
                            .color(party.map(p -> colorOf(p, colors)).orElse(UNKNOWN_PARTY_COLOR))
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

    private static String colorOf(LocalPartyResult party, Map<String, String> colors) {
        final var riksdagColor = colors.get(party.abbreviation());
        if (riksdagColor != null) {
            return riksdagColor;
        }
        return party.color() == null || party.color().isBlank() ? UNKNOWN_PARTY_COLOR : party.color();
    }

}
