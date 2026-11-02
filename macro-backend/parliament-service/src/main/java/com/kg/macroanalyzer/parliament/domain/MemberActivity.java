package com.kg.macroanalyzer.parliament.domain;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record MemberActivity(
        String intressentId,
        int votesYes,
        int votesNo,
        int votesAbsent,
        int votesAbstain,
        List<SessionVotes> votesBySession,
        int speechCount,
        int motionCount,
        List<ParliamentDocument> recentSpeeches,
        List<ParliamentDocument> recentMotions
) {

    public Double attendancePct() {
        final var total = votesYes + votesNo + votesAbsent + votesAbstain;
        if (total == 0) {
            return null;
        }

        return Math.round(1000.0 * (total - votesAbsent) / total) / 10.0;
    }

}
