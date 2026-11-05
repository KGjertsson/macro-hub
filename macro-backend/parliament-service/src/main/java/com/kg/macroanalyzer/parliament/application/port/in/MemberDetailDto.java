package com.kg.macroanalyzer.parliament.application.port.in;

import com.kg.macroanalyzer.parliament.domain.ParliamentDocument;
import com.kg.macroanalyzer.parliament.domain.SessionVotes;
import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
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
        List<SessionVotes> votesBySession,
        Integer speechCount,
        Integer motionCount,
        List<ParliamentDocument> recentSpeeches,
        List<ParliamentDocument> recentMotions
) {

    public MemberDetailDto {
        assignments = ImmutableCopies.list(assignments);
        biography = ImmutableCopies.list(biography);
        votesBySession = ImmutableCopies.list(votesBySession);
        recentSpeeches = ImmutableCopies.list(recentSpeeches);
        recentMotions = ImmutableCopies.list(recentMotions);
    }

}
