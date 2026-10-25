package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.parliament.Election;
import com.kg.macroanalyzer.application.domain.parliament.Party;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kg.macroanalyzer.jooq.generated.Tables.*;

@Repository
public class RiksdagElectionRepository {

    private final DSLContext dslContext;

    @Autowired
    public RiksdagElectionRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<Party> readParties() {
        return dslContext.selectFrom(RIKSDAG_PARTY)
                .fetch()
                .map(r -> Party.builder()
                        .code(r.getCode())
                        .name(r.getName())
                        .color(r.getColor())
                        .ideology(r.getIdeology())
                        .active(r.getActive())
                        .build());
    }

    public List<Election> readElections() {
        final var seatsByYear = dslContext.selectFrom(RIKSDAG_ELECTION_RESULT)
                .orderBy(RIKSDAG_ELECTION_RESULT.SEATS.desc())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        r -> r.getYear(),
                        Collectors.toMap(
                                r -> r.getPartyCode(),
                                r -> r.getSeats(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        )
                ));

        return dslContext.selectFrom(RIKSDAG_ELECTION)
                .orderBy(RIKSDAG_ELECTION.YEAR.desc())
                .fetch()
                .map(r -> Election.builder()
                        .year(r.getYear())
                        .termStart(r.getTermStart())
                        .termEnd(r.getTermEnd())
                        .seatsByParty(seatsByYear.getOrDefault(r.getYear(), new LinkedHashMap<>()))
                        .build());
    }

    public Map<String, String> readOrganNames() {
        return dslContext.selectFrom(RIKSDAG_ORGAN)
                .fetchMap(RIKSDAG_ORGAN.CODE, RIKSDAG_ORGAN.NAME);
    }

}
