package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.application.domain.localelection.Governance;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.parliament.Election;
import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.domain.parliament.Party;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DatabasePort {

    List<MacroSeries> readMacroSeries(List<SeriesConfig> paramList);

    MacroSeries readMacroSeries(SeriesConfig paramList);

    List<SeriesConfig> readSeriesConfigList();

    void persistScrapeQueueItem(ScrapeQueueItem scrapeQueueItem);

    List<ScrapeQueueItem> getScrapeQueue();

    List<ScrapeQueueItem> getItemsToScrape(LocalDateTime timeStamp);

    Integer writeMacroPoints(ConfigWithMacroPoints configWithMacroPoints);

    void markAsDone(SeriesConfig seriesConfig);

    Integer writeMembersOfParliament(List<MemberOfParliament> membersOfParliament);

    List<MemberOfParliament> readMembersOfParliament();

    Integer writeMemberActivity(List<MemberActivity> memberActivity);

    List<MemberActivity> readMemberActivity();

    List<Party> readParties();

    List<Election> readElections();

    Map<String, String> readOrganNames();

    Set<String> readLocalElectionSourceMd5s(LocalElectionType type);

    Integer writeLocalElectionResults(List<LocalElectionResult> results);

    Optional<Integer> readLatestLocalElectionYear(LocalElectionType type);

    List<LocalElectionResult> readLocalElectionResults(LocalElectionType type, int year);

    Optional<LocalElectionResult> readLocalElectionResult(LocalElectionType type, int year, String areaCode);

    Optional<Governance> readGovernance(LocalElectionType type, int year, String areaCode);

}
