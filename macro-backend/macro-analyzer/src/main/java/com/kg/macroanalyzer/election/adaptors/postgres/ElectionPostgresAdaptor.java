package com.kg.macroanalyzer.election.adaptors.postgres;

import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.election.domain.Governance;
import com.kg.macroanalyzer.election.domain.LocalElectionResult;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class ElectionPostgresAdaptor implements ElectionRepositoryPort {

    private final LocalElectionRepository localElectionRepository;

    public ElectionPostgresAdaptor(LocalElectionRepository localElectionRepository) {
        this.localElectionRepository = localElectionRepository;
    }

    @Override
    public Set<String> readLocalElectionSourceMd5s(LocalElectionType type) {
        return localElectionRepository.readSourceMd5s(type);
    }

    @Override
    public Integer writeLocalElectionResults(List<LocalElectionResult> results) {
        return localElectionRepository.writeResults(results);
    }

    @Override
    public Optional<Integer> readLatestLocalElectionYear(LocalElectionType type) {
        return localElectionRepository.readLatestYear(type);
    }

    @Override
    public List<LocalElectionResult> readLocalElectionResults(LocalElectionType type, int year) {
        return localElectionRepository.readResults(type, year);
    }

    @Override
    public Optional<LocalElectionResult> readLocalElectionResult(LocalElectionType type, int year, String areaCode) {
        return localElectionRepository.readResult(type, year, areaCode);
    }

    @Override
    public Optional<Governance> readGovernance(LocalElectionType type, int year, String areaCode) {
        return localElectionRepository.readGovernance(type, year, areaCode);
    }

}
