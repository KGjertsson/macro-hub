package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.kg.macroanalyzer.jooq.generated.Tables.SWEDISH_MEMBERS_OF_PARLIAMENT;

@Repository
public class SweParMemberRepository {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private static final int MAX_NAME_LENGTH = 100;

    private final DSLContext dslContext;

    @Autowired
    public SweParMemberRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<MemberOfParliament> readMembers() {
        return dslContext.select(SWEDISH_MEMBERS_OF_PARLIAMENT.MEMBER_DATA)
                .from(SWEDISH_MEMBERS_OF_PARLIAMENT)
                .fetch()
                .map(r -> JSON_MAPPER.readValue(r.value1().data(), MemberOfParliament.class));
    }

    // Riksdagen returns every member on each scrape, so all rows are upserted every time
    public Integer upsertMembers(List<MemberOfParliament> members) {
        final var queries = members.stream()
                .filter(m -> m.intressent_id() != null && !m.intressent_id().isBlank())
                .map(this::upsertQuery)
                .toList();
        if (queries.isEmpty()) {
            return 0;
        }

        return Arrays.stream(dslContext.batch(queries).execute()).sum();
    }

    private Query upsertQuery(MemberOfParliament member) {
        final var name = truncate(member.fullName());
        final var data = JSONB.valueOf(JSON_MAPPER.writeValueAsString(member));

        return dslContext.insertInto(SWEDISH_MEMBERS_OF_PARLIAMENT)
                .set(SWEDISH_MEMBERS_OF_PARLIAMENT.GLOBAL_ID, UUID.randomUUID())
                .set(SWEDISH_MEMBERS_OF_PARLIAMENT.INTRESSENT_ID, member.intressent_id())
                .set(SWEDISH_MEMBERS_OF_PARLIAMENT.MEMBER_NAME, name)
                .set(SWEDISH_MEMBERS_OF_PARLIAMENT.MEMBER_DATA, data)
                .onConflict(SWEDISH_MEMBERS_OF_PARLIAMENT.INTRESSENT_ID)
                .doUpdate()
                .set(SWEDISH_MEMBERS_OF_PARLIAMENT.MEMBER_NAME, name)
                .set(SWEDISH_MEMBERS_OF_PARLIAMENT.MEMBER_DATA, data);
    }

    private static String truncate(String name) {
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

}
