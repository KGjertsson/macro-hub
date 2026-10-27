package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionDetailDto;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionSummaryDto;
import com.kg.macroanalyzer.application.domain.macroseries.AlignedBundle;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.application.ports.driving.out.OutPort;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.ChartData;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.ChartDataWithLabels;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.CompositionDto;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.MemberSummaryDto;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.PartySeats;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.StrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExtractionAdaptorTest {

    @InjectMocks
    ExtractionAdaptor extractionAdaptor;

    @Mock
    OutPort outPort;
    @Mock
    ColorSelectionStrategy colorSelectionStrategy;

    @Test
    public void shouldReturnOk_whenValidBundleRetrieved() {
        // given
        final List<Temporal> labels = List.of(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3)
        );
        final List<MacroPoint> macroPoints = List.of(
                new MacroPoint(1.0, labels.get(0)),
                new MacroPoint(2.0, labels.get(1)),
                new MacroPoint(3.0, labels.get(2))
        );
        final var macroSeries = List.of(
                new MacroSeries("seriesA", macroPoints)
        );
        final var alignedBundle = AlignedBundle.builder()
                .labels(labels)
                .macroSeries(macroSeries)
                .build();
        final var chartData = macroSeries.stream()
                .map(s -> new ChartData(s.getValues(), s.name(), "rgb(207,82,48)"))
                .toList();
        final var expected = ChartDataWithLabels.builder()
                .labels(labels)
                .chartData(chartData)
                .build();

        // when
        final var params = BuildChartDataParams.builder()
                .strategy(StrategyFactory.Strategy.MONTH)
                .chartSeriesParams(List.of())
                .build();
        when(outPort.buildAlignedBundle(any())).thenReturn(Optional.of(alignedBundle));
        when(colorSelectionStrategy.pickColor(any())).thenReturn("rgb(207,82,48)");
        final var response = extractionAdaptor.buildChartData(params);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    public void shouldReturnBadRequest_whenEmptyRequest() {
        // given
        final var params = BuildChartDataParams.builder().build();

        // when
        final var response = extractionAdaptor.buildChartData(params);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldReturnNoContent_whenEmptyReplyFromServer() {
        // given
        final var params = BuildChartDataParams.builder()
                .strategy(StrategyFactory.Strategy.MONTH)
                .chartSeriesParams(List.of())
                .build();

        // when
        when(outPort.buildAlignedBundle(any())).thenReturn(Optional.empty());
        final var response = extractionAdaptor.buildChartData(params);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnOk_whenMembersFoundForYear() {
        // given
        final var member = MemberSummaryDto.builder().id("1").name("Anna Andersson").party("S").build();

        // when
        when(outPort.getMembersOfParliament(2026)).thenReturn(List.of(member));
        final var response = extractionAdaptor.getMembersOfParliament(2026);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(member), response.getBody());
    }

    @Test
    public void shouldReturnNoContent_whenNoMembersScraped() {
        // when
        when(outPort.getMembersOfParliament(null)).thenReturn(List.of());
        final var response = extractionAdaptor.getMembersOfParliament(null);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnNotFound_whenMemberUnknown() {
        // when
        when(outPort.getMemberOfParliament("missing")).thenReturn(Optional.empty());
        final var response = extractionAdaptor.getMemberOfParliament("missing");

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void shouldReturnComposition_whenServingMembersFound() {
        // given
        final var composition = new CompositionDto("2026-10-25", List.of(new PartySeats("S", "Socialdemokraterna", "#E8112d", 98)));

        // when
        when(outPort.getComposition()).thenReturn(composition);
        final var response = extractionAdaptor.getComposition();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(composition, response.getBody());
    }

    @Test
    public void shouldReturnMunicipalityElections_whenScraped() {
        // given
        final var lund = LocalElectionSummaryDto.builder().code("1281").name("Lund").build();

        // when
        when(outPort.getLocalElectionResults(LocalElectionType.KF, null)).thenReturn(List.of(lund));
        final var response = extractionAdaptor.getMunicipalityElections(null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(lund), response.getBody());
    }

    @Test
    public void shouldReturnNoContent_whenNoRegionElectionsScraped() {
        // when
        when(outPort.getLocalElectionResults(LocalElectionType.RF, null)).thenReturn(List.of());
        final var response = extractionAdaptor.getRegionElections(null);

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
        when(outPort.getLocalElectionResult(LocalElectionType.RF, 2026, "12")).thenReturn(Optional.of(detail));
        final var response = extractionAdaptor.getRegion("12", 2026);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(detail, response.getBody());
    }

    @Test
    public void shouldReturnConstituencyElections_whenScraped() {
        // given
        final var gotland = LocalElectionSummaryDto.builder().code("09").name("Gotlands län").build();

        // when
        when(outPort.getLocalElectionResults(LocalElectionType.RD, null)).thenReturn(List.of(gotland));
        final var response = extractionAdaptor.getConstituencyElections(null);

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
        when(outPort.getLocalElectionResult(LocalElectionType.RD, null, "01")).thenReturn(Optional.of(detail));
        final var response = extractionAdaptor.getConstituency("01", null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(detail, response.getBody());
    }

    @Test
    public void shouldReturnNotFound_whenMunicipalityUnknown() {
        // when
        when(outPort.getLocalElectionResult(LocalElectionType.KF, null, "9999")).thenReturn(Optional.empty());
        final var response = extractionAdaptor.getMunicipality("9999", null);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
