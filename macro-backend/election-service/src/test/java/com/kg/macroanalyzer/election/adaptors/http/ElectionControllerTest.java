package com.kg.macroanalyzer.election.adaptors.http;

import com.kg.macroanalyzer.election.application.port.in.ElectionQueries;
import com.kg.macroanalyzer.election.application.port.in.LocalElectionDetailDto;
import com.kg.macroanalyzer.election.application.port.in.LocalElectionSummaryDto;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
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
public class ElectionControllerTest {

    @InjectMocks
    ElectionController electionController;

    @Mock
    ElectionQueries electionQueries;

    @Test
    public void shouldReturnMunicipalityElections_whenScraped() {
        // given
        final var lund = LocalElectionSummaryDto.builder().code("1281").name("Lund").build();

        // when
        when(electionQueries.getLocalElectionResults(LocalElectionType.KF, null)).thenReturn(List.of(lund));
        final var response = electionController.getMunicipalityElections(null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(lund), response.getBody());
    }

    @Test
    public void shouldReturnNoContent_whenNoRegionElectionsScraped() {
        // when
        when(electionQueries.getLocalElectionResults(LocalElectionType.RF, null)).thenReturn(List.of());
        final var response = electionController.getRegionElections(null);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnRegionDetail_whenFound() {
        // given
        final var detail = LocalElectionDetailDto.builder()
                .summary(LocalElectionSummaryDto.builder().code("12").name("Region Skåne").build())
                .elected(List.of())
                .build();

        // when
        when(electionQueries.getLocalElectionResult(LocalElectionType.RF, 2026, "12")).thenReturn(Optional.of(detail));
        final var response = electionController.getRegion("12", 2026);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(detail, response.getBody());
    }

    @Test
    public void shouldReturnConstituencyElections_whenScraped() {
        // given
        final var gotland = LocalElectionSummaryDto.builder().code("09").name("Gotlands län").build();

        // when
        when(electionQueries.getLocalElectionResults(LocalElectionType.RD, null)).thenReturn(List.of(gotland));
        final var response = electionController.getConstituencyElections(null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(gotland), response.getBody());
    }

    @Test
    public void shouldReturnConstituencyDetail_whenFound() {
        // given
        final var detail = LocalElectionDetailDto.builder()
                .summary(LocalElectionSummaryDto.builder().code("01").name("Stockholms kommun").build())
                .elected(List.of())
                .build();

        // when
        when(electionQueries.getLocalElectionResult(LocalElectionType.RD, null, "01")).thenReturn(Optional.of(detail));
        final var response = electionController.getConstituency("01", null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(detail, response.getBody());
    }

    @Test
    public void shouldReturnNotFound_whenMunicipalityUnknown() {
        // when
        when(electionQueries.getLocalElectionResult(LocalElectionType.KF, null, "9999")).thenReturn(Optional.empty());
        final var response = electionController.getMunicipality("9999", null);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
