package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.domain.parliament.ParliamentDocument;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.kg.macroanalyzer.jooq.generated.Tables.SWEDISH_MEMBER_ACTIVITY;

@Repository
public class SweParActivityRepository {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<ParliamentDocument>> DOCUMENT_LIST = new TypeReference<>() {
    };

    private final DSLContext dslContext;

    @Autowired
    public SweParActivityRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<MemberActivity> readActivity() {
        return dslContext.select()
                .from(SWEDISH_MEMBER_ACTIVITY)
                .fetch()
                .map(SweParActivityRepository::toMemberActivity);
    }

    public Integer upsertActivity(List<MemberActivity> activityList) {
        final var queries = activityList.stream()
                .map(this::upsertQuery)
                .toList();
        if (queries.isEmpty()) {
            return 0;
        }

        return Arrays.stream(dslContext.batch(queries).execute()).sum();
    }

    private Query upsertQuery(MemberActivity activity) {
        final var speeches = toJsonb(activity.recentSpeeches());
        final var motions = toJsonb(activity.recentMotions());

        return dslContext.insertInto(SWEDISH_MEMBER_ACTIVITY)
                .set(SWEDISH_MEMBER_ACTIVITY.INTRESSENT_ID, activity.intressentId())
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_YES, activity.votesYes())
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_NO, activity.votesNo())
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_ABSENT, activity.votesAbsent())
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_ABSTAIN, activity.votesAbstain())
                .set(SWEDISH_MEMBER_ACTIVITY.SPEECH_COUNT, activity.speechCount())
                .set(SWEDISH_MEMBER_ACTIVITY.MOTION_COUNT, activity.motionCount())
                .set(SWEDISH_MEMBER_ACTIVITY.RECENT_SPEECHES, speeches)
                .set(SWEDISH_MEMBER_ACTIVITY.RECENT_MOTIONS, motions)
                .onConflict(SWEDISH_MEMBER_ACTIVITY.INTRESSENT_ID)
                .doUpdate()
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_YES, activity.votesYes())
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_NO, activity.votesNo())
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_ABSENT, activity.votesAbsent())
                .set(SWEDISH_MEMBER_ACTIVITY.VOTES_ABSTAIN, activity.votesAbstain())
                .set(SWEDISH_MEMBER_ACTIVITY.SPEECH_COUNT, activity.speechCount())
                .set(SWEDISH_MEMBER_ACTIVITY.MOTION_COUNT, activity.motionCount())
                .set(SWEDISH_MEMBER_ACTIVITY.RECENT_SPEECHES, speeches)
                .set(SWEDISH_MEMBER_ACTIVITY.RECENT_MOTIONS, motions)
                .set(SWEDISH_MEMBER_ACTIVITY.UPDATED, DSL.val(LocalDateTime.now()));
    }

    private static MemberActivity toMemberActivity(Record r) {
        return MemberActivity.builder()
                .intressentId(r.getValue(SWEDISH_MEMBER_ACTIVITY.INTRESSENT_ID))
                .votesYes(r.getValue(SWEDISH_MEMBER_ACTIVITY.VOTES_YES))
                .votesNo(r.getValue(SWEDISH_MEMBER_ACTIVITY.VOTES_NO))
                .votesAbsent(r.getValue(SWEDISH_MEMBER_ACTIVITY.VOTES_ABSENT))
                .votesAbstain(r.getValue(SWEDISH_MEMBER_ACTIVITY.VOTES_ABSTAIN))
                .speechCount(r.getValue(SWEDISH_MEMBER_ACTIVITY.SPEECH_COUNT))
                .motionCount(r.getValue(SWEDISH_MEMBER_ACTIVITY.MOTION_COUNT))
                .recentSpeeches(fromJsonb(r.getValue(SWEDISH_MEMBER_ACTIVITY.RECENT_SPEECHES)))
                .recentMotions(fromJsonb(r.getValue(SWEDISH_MEMBER_ACTIVITY.RECENT_MOTIONS)))
                .build();
    }

    private static JSONB toJsonb(List<ParliamentDocument> documents) {
        return JSONB.valueOf(JSON_MAPPER.writeValueAsString(documents == null ? List.of() : documents));
    }

    private static List<ParliamentDocument> fromJsonb(JSONB jsonb) {
        return jsonb == null ? List.of() : JSON_MAPPER.readValue(jsonb.data(), DOCUMENT_LIST);
    }

}
