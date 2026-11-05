package com.kg.macroanalyzer.parliament.domain;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;

public record VoteTotals(int yes, int no, int absent, int abstain, List<SessionVotes> bySession) {

    public static final VoteTotals NONE = new VoteTotals(0, 0, 0, 0, List.of());

    public VoteTotals {
        bySession = ImmutableCopies.list(bySession);
    }

    public static VoteTotals of(List<SessionVotes> bySession) {
        return new VoteTotals(
                bySession.stream().mapToInt(SessionVotes::yes).sum(),
                bySession.stream().mapToInt(SessionVotes::no).sum(),
                bySession.stream().mapToInt(SessionVotes::absent).sum(),
                bySession.stream().mapToInt(SessionVotes::abstain).sum(),
                bySession
        );
    }

}
