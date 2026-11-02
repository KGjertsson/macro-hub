package com.kg.macroanalyzer.election.application.port.out;

import com.kg.macroanalyzer.election.domain.Governance;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.election.domain.LocalElectionType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ElectionRepositoryPort {

    Set<String> readLocalElectionSourceMd5s(LocalElectionType type);

    Integer writeLocalElectionResults(List<LocalElectionResult> results);

    Optional<Integer> readLatestLocalElectionYear(LocalElectionType type);

    List<LocalElectionResult> readLocalElectionResults(LocalElectionType type, int year);

    Optional<LocalElectionResult> readLocalElectionResult(LocalElectionType type, int year, String areaCode);

    Optional<Governance> readGovernance(LocalElectionType type, int year, String areaCode);

}
