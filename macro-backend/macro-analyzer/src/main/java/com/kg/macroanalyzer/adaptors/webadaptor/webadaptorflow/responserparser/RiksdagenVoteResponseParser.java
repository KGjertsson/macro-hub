package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.kg.macroanalyzer.application.domain.parliament.VoteTotals;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;

// Parses voteringlista grouped by name, which holds one row of career totals per member
public record RiksdagenVoteResponseParser() implements ResponseParser<String, VoteTotals> {

    @Override
    public VoteTotals parse(String response) throws ScrapeException {
        final var root = RiksdagenJson.readTree(response, "voteringlista");
        final var rows = RiksdagenJson.asList(root.path("voteringlista").path("votering"));
        if (rows.isEmpty()) {
            return VoteTotals.NONE;
        }
        final var row = rows.getFirst();

        return new VoteTotals(
                row.path("Ja").asInt(0),
                row.path("Nej").asInt(0),
                row.path("Frånvarande").asInt(0),
                row.path("Avstår").asInt(0)
        );
    }

}
