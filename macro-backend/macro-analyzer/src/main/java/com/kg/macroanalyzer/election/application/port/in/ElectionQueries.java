package com.kg.macroanalyzer.election.application.port.in;

import com.kg.macroanalyzer.election.domain.LocalElectionType;

import java.util.List;
import java.util.Optional;

public interface ElectionQueries {

    List<LocalElectionSummaryDto> getLocalElectionResults(LocalElectionType type, Integer year);

    Optional<LocalElectionDetailDto> getLocalElectionResult(LocalElectionType type, Integer year, String areaCode);

}
