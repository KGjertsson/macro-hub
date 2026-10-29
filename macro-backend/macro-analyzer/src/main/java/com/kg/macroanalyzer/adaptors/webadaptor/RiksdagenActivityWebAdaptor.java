package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.RiksdagenUrlConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenMotionResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenSpeechResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenVoteResponseParser;
import com.kg.macroanalyzer.application.domain.parliament.DocumentPage;
import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.domain.parliament.VoteTotals;
import com.kg.macroanalyzer.application.ports.driven.MemberActivityRequest;
import com.kg.macroanalyzer.shared.web.HttpGetter;
import com.kg.macroanalyzer.shared.web.ResponseParser;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import com.kg.macroanalyzer.shared.web.WebPort;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RiksdagenActivityWebAdaptor implements WebPort<MemberActivityRequest, MemberActivity> {

    private static final int RECENT_COUNT = 5;
    private static final int MAX_SPEECHES = 20_000;
    private static final int ATTEMPTS = 2;
    private static final long RETRY_DELAY_MS = 1_000;

    private final WebAdaptorFlow<String, VoteTotals> voteFlow = flow(new RiksdagenVoteResponseParser());
    private final WebAdaptorFlow<String, DocumentPage> speechFlow = flow(new RiksdagenSpeechResponseParser(RECENT_COUNT));
    private final WebAdaptorFlow<String, DocumentPage> motionFlow = flow(new RiksdagenMotionResponseParser());

    @Override
    public MemberActivity fetch(MemberActivityRequest request) throws ScrapeException {
        final var base = request.baseUrl();
        final var iid = URLEncoder.encode(request.intressentId(), StandardCharsets.UTF_8);

        final var votes = fetchWithRetry(voteFlow,
                "%s/voteringlista/?iid=%s&utformat=json&gruppering=rm&sz=500".formatted(base, iid));
        final var speeches = fetchWithRetry(speechFlow,
                "%s/anforandelista/?iid=%s&utformat=json&sz=%d".formatted(base, iid, MAX_SPEECHES));
        final var motions = fetchWithRetry(motionFlow,
                "%s/dokumentlista/?iid=%s&doktyp=mot&utformat=json&sort=datum&sortorder=desc&sz=%d"
                        .formatted(base, iid, RECENT_COUNT));

        return MemberActivity.builder()
                .intressentId(request.intressentId())
                .votesYes(votes.yes())
                .votesNo(votes.no())
                .votesAbsent(votes.absent())
                .votesAbstain(votes.abstain())
                .votesBySession(votes.bySession())
                .speechCount(speeches.total())
                .motionCount(motions.total())
                .recentSpeeches(speeches.recent())
                .recentMotions(motions.recent())
                .build();
    }

    // Riksdagen occasionally resets connections, so each call gets one retry
    private static <T> T fetchWithRetry(WebAdaptorFlow<String, T> flow, String url) throws ScrapeException {
        ScrapeException lastException = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                return flow.fetch(url);
            } catch (ScrapeException e) {
                lastException = e;
                log.warn("Attempt %d failed for %s: %s".formatted(attempt, url, e.getMessage()));
                if (attempt < ATTEMPTS) {
                    sleep(RETRY_DELAY_MS);
                }
            }
        }

        throw lastException;
    }

    private static <T> WebAdaptorFlow<String, T> flow(ResponseParser<String, T> parser) {
        return new WebAdaptorFlow<>(new RiksdagenUrlConnectionBuilder(), new HttpGetter(), parser);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
