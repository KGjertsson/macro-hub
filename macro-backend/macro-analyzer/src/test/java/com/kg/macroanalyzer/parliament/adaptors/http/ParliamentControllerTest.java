package com.kg.macroanalyzer.parliament.adaptors.http;

import com.kg.macroanalyzer.parliament.application.port.in.CompositionDto;
import com.kg.macroanalyzer.parliament.application.port.in.MemberDetailDto;
import com.kg.macroanalyzer.parliament.application.port.in.MemberSummaryDto;
import com.kg.macroanalyzer.parliament.application.port.in.ParliamentQueries;
import com.kg.macroanalyzer.parliament.application.port.in.PartyDto;
import com.kg.macroanalyzer.parliament.application.port.in.PartySeats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParliamentControllerTest {

    @InjectMocks
    ParliamentController parliamentController;

    @Mock
    ParliamentQueries parliamentQueries;

    @Test
    public void shouldReturnOk_whenMembersFoundForYear() {
        // given
        final var member = MemberSummaryDto.builder().id("1").name("Anna Andersson").party("S").build();

        // when
        when(parliamentQueries.getMembersOfParliament(2026)).thenReturn(List.of(member));
        final var response = parliamentController.getMembersOfParliament(2026);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(member), response.getBody());
    }

    @Test
    public void shouldReturnNoContent_whenNoMembersScraped() {
        // when
        when(parliamentQueries.getMembersOfParliament(null)).thenReturn(List.of());
        final var response = parliamentController.getMembersOfParliament(null);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnOk_whenMemberFound() {
        // given
        final var summary = MemberSummaryDto.builder().id("0123456789").name("Anna Andersson").party("S").build();
        final var member = MemberDetailDto.builder().summary(summary).birthYear(1970).speechCount(12).build();

        // when
        when(parliamentQueries.getMemberOfParliament("0123456789")).thenReturn(Optional.of(member));
        final var response = parliamentController.getMemberOfParliament("0123456789");

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(member, response.getBody());
    }

    @Test
    public void shouldReturnNotFound_whenMemberUnknown() {
        // when
        when(parliamentQueries.getMemberOfParliament("missing")).thenReturn(Optional.empty());
        final var response = parliamentController.getMemberOfParliament("missing");

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void shouldReturnComposition_whenServingMembersFound() {
        // given
        final var composition = new CompositionDto("2026-10-25", List.of(new PartySeats("S", "Socialdemokraterna", "#E8112d", 98)));

        // when
        when(parliamentQueries.getComposition()).thenReturn(composition);
        final var response = parliamentController.getComposition();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(composition, response.getBody());
    }

    @Test
    public void shouldReturnNoContent_whenNoServingMembers() {
        // when
        when(parliamentQueries.getComposition()).thenReturn(new CompositionDto("2026-10-25", List.of()));
        final var response = parliamentController.getComposition();

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnParties() {
        // given
        final var party = new PartyDto("S", "Socialdemokraterna", "#E8112d", "Social Democracy", true);

        // when
        when(parliamentQueries.getParties()).thenReturn(List.of(party));
        final var response = parliamentController.getParties();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(party), response.getBody());
    }

    @Test
    public void shouldReturnNoContent_whenNoElections() {
        // when
        when(parliamentQueries.getElections()).thenReturn(List.of());
        final var response = parliamentController.getElections();

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
