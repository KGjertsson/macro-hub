package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.localelection.Governance;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DatabasePort {

    List<MacroSeries> readMacroSeries(List<SeriesConfig> paramList);

    MacroSeries readMacroSeries(SeriesConfig paramList);

    Integer writeMacroPoints(ConfigWithMacroPoints configWithMacroPoints);

    Set<String> readLocalElectionSourceMd5s(LocalElectionType type);

    Integer writeLocalElectionResults(List<LocalElectionResult> results);

    Optional<Integer> readLatestLocalElectionYear(LocalElectionType type);

    List<LocalElectionResult> readLocalElectionResults(LocalElectionType type, int year);

    Optional<LocalElectionResult> readLocalElectionResult(LocalElectionType type, int year, String areaCode);

    Optional<Governance> readGovernance(LocalElectionType type, int year, String areaCode);

}
