package com.kg.macroanalyzer.application.domain.localelection;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder(toBuilder = true)
public record LocalElectionResult(
        int year,
        LocalElectionType type,
        String areaCode,
        String areaName,
        String lanCode,
        int totalSeats,
        BigDecimal turnout,
        Integer eligibleVoters,
        Integer totalVotes,
        List<LocalPartyResult> parties,
        List<ElectedMember> elected,
        String sourceMd5
) {
}
