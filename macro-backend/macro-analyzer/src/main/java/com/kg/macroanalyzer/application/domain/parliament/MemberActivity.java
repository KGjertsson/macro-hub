package com.kg.macroanalyzer.application.domain.parliament;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record MemberActivity(
        String intressentId,
        int votesYes,
        int votesNo,
        int votesAbsent,
        int votesAbstain,
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
