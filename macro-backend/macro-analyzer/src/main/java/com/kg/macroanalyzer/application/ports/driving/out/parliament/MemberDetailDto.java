package com.kg.macroanalyzer.application.ports.driving.out.parliament;

import com.kg.macroanalyzer.application.domain.parliament.ParliamentDocument;
import lombok.Builder;

import java.util.List;

@Builder
public record MemberDetailDto(
        MemberSummaryDto summary,
        String imageUrlLarge,
        Integer birthYear,
        String gender,
        String residence,
        Boolean runningNextElection,
        ContactDto contact,
        List<AssignmentDto> assignments,
        List<BiographyEntryDto> biography,
        VotesDto votes,
        Integer speechCount,
        Integer motionCount,
        List<ParliamentDocument> recentSpeeches,
        List<ParliamentDocument> recentMotions
) {
}
