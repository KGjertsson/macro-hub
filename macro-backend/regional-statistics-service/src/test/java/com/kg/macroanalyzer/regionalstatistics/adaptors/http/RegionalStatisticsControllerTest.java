package com.kg.macroanalyzer.regionalstatistics.adaptors.http;

import com.kg.macroanalyzer.regionalstatistics.application.port.in.AvailabilityDto;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalIndicatorDto;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalStatisticsQueries;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalValuesDto;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegionalStatisticsControllerTest {

    @InjectMocks
    RegionalStatisticsController regionalStatisticsController;

    @Mock
    RegionalStatisticsQueries regionalStatisticsQueries;

    @Test
    public void shouldReturnIndicators() {
        // given
        final var indicator = RegionalIndicatorDto.builder()
                .id("population")
                .availability(Map.of("DeSO", List.of(new AvailabilityDto(2025, "DeSO2025")), "RegSO", List.of()))
                .build();

        // when
        when(regionalStatisticsQueries.getRegionalIndicators()).thenReturn(List.of(indicator));
        final var response = regionalStatisticsController.getRegionalIndicators();

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(indicator), response.getBody());
    }

    @Test
    public void shouldReturnRegionalValues_whenFound() {
        // given
        final var values = RegionalValuesDto.builder()
                .indicator("population")
                .level("DeSO")
                .year(2025)
                .geoVersion("DeSO2025")
                .values(Map.of("0114A0010", 1565.0))
                .build();

        // when
        when(regionalStatisticsQueries.getRegionalValues("population", ScbLevel.DESO, null)).thenReturn(Optional.of(values));
        final var response = regionalStatisticsController.getRegionalValues("population", "DeSO", null);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(values, response.getBody());
    }

    @Test
    public void shouldReturnNoContent_whenNoRegionalValues() {
        // when
        when(regionalStatisticsQueries.getRegionalValues("population", ScbLevel.REGSO, 2010)).thenReturn(Optional.empty());
        final var response = regionalStatisticsController.getRegionalValues("population", "RegSO", 2010);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldReturnBadRequest_whenLevelUnknown() {
        // when
        final var response = regionalStatisticsController.getRegionalValues("population", "Kommun", null);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(regionalStatisticsQueries);
    }

}
