package com.kg.macroanalyzer.application.domain.parliament;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.kg.macroanalyzer.TestMemberFactory.chamberSeat;
import static org.junit.jupiter.api.Assertions.*;

public class UppdragTest {

    private static final LocalDate TERM_START = LocalDate.parse("2026-09-28");
    private static final LocalDate TERM_END = LocalDate.parse("2030-09-23");

    @Test
    void shouldParseBothRiksdagenDateFormats() {
        // given
        final var uppdrag = chamberSeat("2026-09-28 11:00:00", "2030-09-23");

        // then
        assertEquals(LocalDate.parse("2026-09-28"), uppdrag.fromDate());
        assertEquals(LocalDate.parse("2030-09-23"), uppdrag.tomDate());
    }

    @Test
    void shouldTreatMissingEndDateAsOpenEnded() {
        // given
        final var uppdrag = chamberSeat("2026-09-28", "");

        // then
        assertEquals(LocalDate.MAX, uppdrag.tomDate());
        assertTrue(uppdrag.activeOn(LocalDate.parse("2040-01-01")));
    }

    @Test
    void shouldNotCountPreviousTerm_whenItEndsOnTheNewTermsFirstDay() {
        // given
        final var previousTerm = chamberSeat("2022-09-26 11:00:00", "2026-09-28 10:59:00");
        final var currentTerm = chamberSeat("2026-09-28 11:00:00", "2030-09-23 10:59:00");

        // then
        assertFalse(previousTerm.overlaps(TERM_START, TERM_END));
        assertTrue(currentTerm.overlaps(TERM_START, TERM_END));
    }

}
