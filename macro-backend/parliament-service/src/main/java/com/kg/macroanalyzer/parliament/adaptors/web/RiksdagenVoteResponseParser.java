package com.kg.macroanalyzer.parliament.adaptors.web;

import com.kg.macroanalyzer.parliament.domain.SessionVotes;
import com.kg.macroanalyzer.parliament.domain.VoteTotals;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.util.Comparator;

// Parses voteringlista grouped by rm, which holds one row per riksmöte for the member; career totals are their sum
public record RiksdagenVoteResponseParser() implements ResponseParser<String, VoteTotals> {

    @Override
    public VoteTotals parse(String response) throws ScrapeException {
        final var root = RiksdagenJson.readTree(response, "voteringlista");
        final var rows = RiksdagenJson.asList(root.path("voteringlista").path("votering"));
        if (rows.isEmpty()) {
            return VoteTotals.NONE;
        }

        return VoteTotals.of(rows.stream()
                .map(row -> new SessionVotes(
                        row.path("rm").asString(""),
                        row.path("Ja").asInt(0),
                        row.path("Nej").asInt(0),
                        row.path("Frånvarande").asInt(0),
                        row.path("Avstår").asInt(0)
                ))
                .filter(s -> !s.riksmote().isBlank())
                .sorted(Comparator.comparing(SessionVotes::riksmote))
                .toList());
    }

}
