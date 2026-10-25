package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.localelection.ElectedMember;
import com.kg.macroanalyzer.application.domain.localelection.Governance;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.localelection.LocalPartyResult;
import com.kg.macroanalyzer.jooq.generated.tables.records.LocalElectionAreaRecord;
import com.kg.macroanalyzer.jooq.generated.tables.records.LocalElectionPartyResultRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kg.macroanalyzer.jooq.generated.Tables.LOCAL_ELECTION_AREA;
import static com.kg.macroanalyzer.jooq.generated.Tables.LOCAL_ELECTION_PARTY_RESULT;
import static com.kg.macroanalyzer.jooq.generated.Tables.LOCAL_GOVERNANCE;

@Repository
public class LocalElectionRepository {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<ElectedMember>> MEMBER_LIST = new TypeReference<>() {
    };

    private final DSLContext dslContext;

    @Autowired
    public LocalElectionRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Set<String> readSourceMd5s(LocalElectionType type) {
        return new HashSet<>(dslContext.select(LOCAL_ELECTION_AREA.SOURCE_MD5)
                .from(LOCAL_ELECTION_AREA)
                .where(LOCAL_ELECTION_AREA.ELECTION_TYPE.eq(type.name()))
                .fetch(LOCAL_ELECTION_AREA.SOURCE_MD5));
    }

    // Each area is replaced as a whole so parties that lost all votes in a corrected file disappear
    public Integer writeResults(List<LocalElectionResult> results) {
        results.forEach(result -> dslContext.transaction(configuration -> {
            final var tx = DSL.using(configuration);
            tx.insertInto(LOCAL_ELECTION_AREA)
                    .set(areaRecord(result))
                    .onConflict(LOCAL_ELECTION_AREA.ELECTION_YEAR, LOCAL_ELECTION_AREA.ELECTION_TYPE, LOCAL_ELECTION_AREA.AREA_CODE)
                    .doUpdate()
                    .set(areaRecord(result))
                    .execute();
            tx.deleteFrom(LOCAL_ELECTION_PARTY_RESULT)
                    .where(areaCondition(result.year(), result.type(), result.areaCode()))
                    .execute();
            tx.batchInsert(result.parties().stream()
                            .map(p -> partyRecord(result, p))
                            .toList())
                    .execute();
        }));

        return results.size();
    }

    public Optional<Integer> readLatestYear(LocalElectionType type) {
        return dslContext.select(DSL.max(LOCAL_ELECTION_AREA.ELECTION_YEAR))
                .from(LOCAL_ELECTION_AREA)
                .where(LOCAL_ELECTION_AREA.ELECTION_TYPE.eq(type.name()))
                .fetchOptional(0, Integer.class);
    }

    // Elected members are left out of the list to keep the overview payload small
    public List<LocalElectionResult> readResults(LocalElectionType type, int year) {
        final var partiesByArea = dslContext.selectFrom(LOCAL_ELECTION_PARTY_RESULT)
                .where(LOCAL_ELECTION_PARTY_RESULT.ELECTION_TYPE.eq(type.name()))
                .and(LOCAL_ELECTION_PARTY_RESULT.ELECTION_YEAR.eq(year))
                .orderBy(LOCAL_ELECTION_PARTY_RESULT.VOTES.desc())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        LocalElectionPartyResultRecord::getAreaCode,
                        Collectors.mapping(LocalElectionRepository::toPartyResult, Collectors.toList())
                ));

        return dslContext.selectFrom(LOCAL_ELECTION_AREA)
                .where(LOCAL_ELECTION_AREA.ELECTION_TYPE.eq(type.name()))
                .and(LOCAL_ELECTION_AREA.ELECTION_YEAR.eq(year))
                .orderBy(LOCAL_ELECTION_AREA.AREA_NAME)
                .fetch()
                .map(r -> toResult(r, partiesByArea.getOrDefault(r.getAreaCode(), List.of()), List.of()));
    }

    public Optional<LocalElectionResult> readResult(LocalElectionType type, int year, String areaCode) {
        final var parties = dslContext.selectFrom(LOCAL_ELECTION_PARTY_RESULT)
                .where(areaCondition(year, type, areaCode))
                .orderBy(LOCAL_ELECTION_PARTY_RESULT.VOTES.desc())
                .fetch()
                .map(LocalElectionRepository::toPartyResult);

        return dslContext.selectFrom(LOCAL_ELECTION_AREA)
                .where(LOCAL_ELECTION_AREA.ELECTION_TYPE.eq(type.name()))
                .and(LOCAL_ELECTION_AREA.ELECTION_YEAR.eq(year))
                .and(LOCAL_ELECTION_AREA.AREA_CODE.eq(areaCode))
                .fetchOptional()
                .map(r -> toResult(r, parties, fromJsonb(r.getElected())));
    }

    public Optional<Governance> readGovernance(LocalElectionType type, int year, String areaCode) {
        return dslContext.selectFrom(LOCAL_GOVERNANCE)
                .where(LOCAL_GOVERNANCE.ELECTION_TYPE.eq(type.name()))
                .and(LOCAL_GOVERNANCE.ELECTION_YEAR.eq(year))
                .and(LOCAL_GOVERNANCE.AREA_CODE.eq(areaCode))
                .fetchOptional()
                .map(r -> Governance.builder()
                        .year(r.getElectionYear())
                        .type(LocalElectionType.valueOf(r.getElectionType()))
                        .areaCode(r.getAreaCode())
                        .coalitionName(r.getCoalitionName())
                        .description(r.getDescription())
                        .governingParties(Arrays.asList(r.getGoverningParties()))
                        .supportParties(Arrays.asList(r.getSupportParties()))
                        .build());
    }

    private static Condition areaCondition(int year, LocalElectionType type, String areaCode) {
        return LOCAL_ELECTION_PARTY_RESULT.ELECTION_YEAR.eq(year)
                .and(LOCAL_ELECTION_PARTY_RESULT.ELECTION_TYPE.eq(type.name()))
                .and(LOCAL_ELECTION_PARTY_RESULT.AREA_CODE.eq(areaCode));
    }

    private static LocalElectionAreaRecord areaRecord(LocalElectionResult result) {
        final var record = new LocalElectionAreaRecord();
        record.setElectionYear(result.year());
        record.setElectionType(result.type().name());
        record.setAreaCode(result.areaCode());
        record.setAreaName(result.areaName());
        record.setLanCode(result.lanCode());
        record.setTotalSeats(result.totalSeats());
        record.setTurnout(result.turnout());
        record.setEligibleVoters(result.eligibleVoters());
        record.setTotalVotes(result.totalVotes());
        record.setElected(toJsonb(result.elected()));
        record.setSourceMd5(result.sourceMd5());
        record.setUpdated(LocalDateTime.now());

        return record;
    }

    private static LocalElectionPartyResultRecord partyRecord(LocalElectionResult result, LocalPartyResult party) {
        return new LocalElectionPartyResultRecord(
                result.year(),
                result.type().name(),
                result.areaCode(),
                party.code(),
                party.abbreviation(),
                party.name(),
                party.color(),
                party.votes(),
                party.voteShare(),
                party.seats()
        );
    }

    private static LocalPartyResult toPartyResult(LocalElectionPartyResultRecord r) {
        return LocalPartyResult.builder()
                .code(r.getPartyCode())
                .abbreviation(r.getPartyAbbr())
                .name(r.getPartyName())
                .color(r.getColor())
                .votes(r.getVotes())
                .voteShare(r.getVoteShare())
                .seats(r.getSeats())
                .build();
    }

    private static LocalElectionResult toResult(
            LocalElectionAreaRecord r,
            List<LocalPartyResult> parties,
            List<ElectedMember> elected
    ) {
        return LocalElectionResult.builder()
                .year(r.getElectionYear())
                .type(LocalElectionType.valueOf(r.getElectionType()))
                .areaCode(r.getAreaCode())
                .areaName(r.getAreaName())
                .lanCode(r.getLanCode())
                .totalSeats(r.getTotalSeats())
                .turnout(r.getTurnout())
                .eligibleVoters(r.getEligibleVoters())
                .totalVotes(r.getTotalVotes())
                .parties(parties)
                .elected(elected)
                .sourceMd5(r.getSourceMd5())
                .build();
    }

    private static JSONB toJsonb(List<ElectedMember> members) {
        return JSONB.valueOf(JSON_MAPPER.writeValueAsString(members == null ? List.of() : members));
    }

    private static List<ElectedMember> fromJsonb(JSONB jsonb) {
        return jsonb == null ? List.of() : JSON_MAPPER.readValue(jsonb.data(), MEMBER_LIST);
    }

}
