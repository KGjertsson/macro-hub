package com.kg.macroanalyzer.application.ports.driving.out;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.macroseries.AlignedBundle;
import com.kg.macroanalyzer.application.domain.scb.ScbLevel;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionDetailDto;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionSummaryDto;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.*;
import com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics.RegionalIndicatorDto;
import com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics.RegionalValuesDto;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;
import java.util.Optional;

public interface OutPort {

    Optional<AlignedBundle> buildAlignedBundle(BuildChartDataParams params);

    List<SeriesConfig> getSeriesConfigList();

    List<SeriesConfig> getChartSeriesConfigList();

    List<PartyDto> getParties();

    List<ElectionResultDto> getElections();

    CompositionDto getComposition();

    List<MemberSummaryDto> getMembersOfParliament(Integer electionYear);

    Optional<MemberDetailDto> getMemberOfParliament(String id);

    List<LocalElectionSummaryDto> getLocalElectionResults(LocalElectionType type, Integer year);

    Optional<LocalElectionDetailDto> getLocalElectionResult(LocalElectionType type, Integer year, String areaCode);

    List<RegionalIndicatorDto> getRegionalIndicators();

    Optional<RegionalValuesDto> getRegionalValues(String indicatorId, ScbLevel level, Integer year);

}
