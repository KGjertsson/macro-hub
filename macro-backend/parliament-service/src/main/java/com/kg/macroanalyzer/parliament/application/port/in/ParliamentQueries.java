package com.kg.macroanalyzer.parliament.application.port.in;

import java.util.List;
import java.util.Optional;

public interface ParliamentQueries {

    List<PartyDto> getParties();

    List<ElectionResultDto> getElections();

    CompositionDto getComposition();

    List<MemberSummaryDto> getMembersOfParliament(Integer electionYear);

    Optional<MemberDetailDto> getMemberOfParliament(String id);

}
