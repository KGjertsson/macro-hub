package com.kg.macroanalyzer.election.adaptors.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshingPartyColorAdaptorTest {

    private static final Map<String, String> COLORS = Map.of("SD", "#FFCC00");
    private static final Duration SOON = Duration.ofMillis(10);
    private static final Duration NOT_DURING_THE_TEST = Duration.ofHours(1);

    @Mock
    ParliamentPartiesClient client;

    RefreshingPartyColorAdaptor adaptor;

    @AfterEach
    void closeAdaptor() {
        adaptor.close();
    }

    @Test
    void shouldHoldNoColorsAndCallNothing_beforeTheApplicationIsReady() throws Exception {
        // when
        adaptor = adaptor(SOON, SOON);

        // then
        assertEquals(Map.of(), adaptor.partyColors());
        Thread.sleep(50);
        verify(client, never()).fetchPartyColors();
    }

    @Test
    void shouldLoadColors_rightAfterStart_withoutWaitingForAnInterval() throws Exception {
        // given
        when(client.fetchPartyColors()).thenReturn(COLORS);
        adaptor = adaptor(NOT_DURING_THE_TEST, NOT_DURING_THE_TEST);

        // when
        adaptor.start();

        // then
        await().atMost(Duration.ofSeconds(5)).until(() -> COLORS.equals(adaptor.partyColors()));
    }

    @Test
    void shouldRetrySoon_whileThereIsNoList() throws Exception {
        // given
        when(client.fetchPartyColors())
                .thenThrow(new IOException("down"))
                .thenReturn(Map.of())
                .thenReturn(COLORS);
        adaptor = adaptor(NOT_DURING_THE_TEST, SOON);

        // when
        adaptor.start();

        // then
        await().atMost(Duration.ofSeconds(5)).until(() -> COLORS.equals(adaptor.partyColors()));
    }

    @Test
    void shouldWaitForTheRefreshInterval_onceItHasAList() throws Exception {
        // given
        when(client.fetchPartyColors()).thenReturn(COLORS);
        adaptor = adaptor(NOT_DURING_THE_TEST, SOON);

        // when
        adaptor.start();
        await().atMost(Duration.ofSeconds(5)).until(() -> COLORS.equals(adaptor.partyColors()));
        Thread.sleep(100);

        // then
        verify(client, times(1)).fetchPartyColors();
    }

    @Test
    void shouldReplaceTheList_onALaterRefresh() throws Exception {
        // given
        when(client.fetchPartyColors())
                .thenReturn(COLORS)
                .thenReturn(Map.of("SD", "#000000"));
        adaptor = adaptor(SOON, SOON);

        // when
        adaptor.start();

        // then
        await().atMost(Duration.ofSeconds(5)).until(() -> "#000000".equals(adaptor.partyColors().get("SD")));
    }

    @Test
    void shouldKeepTheList_whenARefreshFailsOrFindsNoParties_andKeepRefreshing() throws Exception {
        // given
        when(client.fetchPartyColors())
                .thenReturn(COLORS)
                .thenThrow(new IOException("down"))
                .thenThrow(new IllegalStateException("unexpected"))
                .thenReturn(Map.of());
        adaptor = adaptor(SOON, SOON);

        // when
        adaptor.start();
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(client, atLeast(5)).fetchPartyColors());

        // then
        assertEquals(COLORS, adaptor.partyColors());
    }

    @Test
    void shouldAnswerAtOnce_whileARefreshHangs() throws Exception {
        // given
        final var entered = new CountDownLatch(1);
        when(client.fetchPartyColors()).thenAnswer(invocation -> {
            entered.countDown();
            new CountDownLatch(1).await();
            return COLORS;
        });
        adaptor = adaptor(SOON, SOON);

        // when
        assertTimeout(Duration.ofSeconds(1), adaptor::start);
        entered.await();

        // then
        assertTimeout(Duration.ofSeconds(1), () -> assertEquals(Map.of(), adaptor.partyColors()));
    }

    @Test
    void shouldStopRefreshing_whenClosed() throws Exception {
        // given
        when(client.fetchPartyColors()).thenReturn(COLORS);
        adaptor = adaptor(SOON, SOON);
        adaptor.start();
        await().atMost(Duration.ofSeconds(5)).until(() -> COLORS.equals(adaptor.partyColors()));

        // when
        adaptor.close();
        Thread.sleep(50);
        clearInvocations(client);
        Thread.sleep(50);

        // then
        verify(client, never()).fetchPartyColors();
    }

    @Test
    void shouldRejectAnIntervalThatIsNotPositive() {
        adaptor = adaptor(SOON, SOON);

        assertThrows(IllegalArgumentException.class, () -> adaptor(Duration.ZERO, SOON));
        assertThrows(IllegalArgumentException.class, () -> adaptor(SOON, Duration.ZERO));
    }

    private RefreshingPartyColorAdaptor adaptor(Duration refreshInterval, Duration retryInterval) {
        return new RefreshingPartyColorAdaptor(client, refreshInterval, retryInterval);
    }

}
