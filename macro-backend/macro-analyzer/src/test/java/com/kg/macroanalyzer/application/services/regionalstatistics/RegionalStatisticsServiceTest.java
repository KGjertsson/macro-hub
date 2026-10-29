package com.kg.macroanalyzer.application.services.regionalstatistics;

import com.kg.macroanalyzer.application.domain.scb.GeoVersion;
import com.kg.macroanalyzer.application.domain.scb.ScbAvailability;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbLevel;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.regionalstatistics.AvailabilityDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegionalStatisticsServiceTest {

    private static final List<ScbAvailability> POPULATION = List.of(
            new ScbAvailability("population", GeoVersion.DESO_2018, 2022),
            new ScbAvailability("population", GeoVersion.DESO_2018, 2023),
            new ScbAvailability("population", GeoVersion.DESO_2025, 2024),
            // Both versions for one year: the newer one wins
            new ScbAvailability("population", GeoVersion.DESO_2018, 2025),
            new ScbAvailability("population", GeoVersion.DESO_2025, 2025),
            new ScbAvailability("population", GeoVersion.REGSO_2020, 2023)
    );

    RegionalStatisticsService service;

    @Mock
    DatabasePort databasePort;

    @BeforeEach
    void setUp() {
        service = new RegionalStatisticsService(databasePort);
    }

    @Test
    void shouldListIndicators_withAvailabilityPerLevel_newestYearFirst() {
        // given
        final var population = ScbIndicator.builder()
                .id("population").label("Folkmängd").description("d").category("Befolkning").unit("antal").decimals(0)
                .build();
        final var turnout = ScbIndicator.builder()
                .id("turnout_riksdag").label("Valdeltagande").description("d").category("Val").unit("%").decimals(1)
                .build();

        // when
        when(databasePort.readScbIndicators()).thenReturn(List.of(population, turnout));
        when(databasePort.readScbAvailability()).thenReturn(POPULATION);
        final var indicators = service.getIndicators();

        // then
        assertEquals(List.of("population", "turnout_riksdag"), indicators.stream().map(i -> i.id()).toList());
        assertEquals(
                Map.of(
                        "DeSO", List.of(
                                new AvailabilityDto(2025, "DeSO2025"),
                                new AvailabilityDto(2024, "DeSO2025"),
                                new AvailabilityDto(2023, "DeSO2018"),
                                new AvailabilityDto(2022, "DeSO2018")),
                        "RegSO", List.of(new AvailabilityDto(2023, "RegSO2020"))
                ),
                indicators.getFirst().availability()
        );
        assertEquals(Map.of("DeSO", List.of(), "RegSO", List.of()), indicators.get(1).availability());
        assertEquals(List.of("DeSO", "RegSO"), List.copyOf(indicators.getFirst().availability().keySet()));
    }

    @Test
    void shouldUseTheLatestYear_andTheNewerVersion_whenNoYearIsGiven() {
        // when
        when(databasePort.readScbAvailability("population")).thenReturn(POPULATION);
        when(databasePort.readScbValues("population", GeoVersion.DESO_2025, 2025)).thenReturn(Map.of("0114A0010", 1565.0));
        final var values = service.getValues("population", ScbLevel.DESO, null).orElseThrow();

        // then
        assertEquals("population", values.indicator());
        assertEquals("DeSO", values.level());
        assertEquals(2025, values.year());
        assertEquals("DeSO2025", values.geoVersion());
        assertEquals(Map.of("0114A0010", 1565.0), values.values());
    }

    @Test
    void shouldUseTheVersionOfTheRequestedYear() {
        // when
        when(databasePort.readScbAvailability("population")).thenReturn(POPULATION);
        when(databasePort.readScbValues("population", GeoVersion.DESO_2018, 2023)).thenReturn(Map.of("0114A0010", 1500.0));
        final var values = service.getValues("population", ScbLevel.DESO, 2023).orElseThrow();

        // then
        assertEquals("DeSO2018", values.geoVersion());
    }

    @Test
    void shouldReturnEmpty_whenTheLevelOrYearHasNoData() {
        // when
        when(databasePort.readScbAvailability(anyString())).thenReturn(POPULATION);

        // then
        assertTrue(service.getValues("population", ScbLevel.DESO, 2010).isEmpty());
        assertTrue(service.getValues("population", ScbLevel.REGSO, 2024).isEmpty());
        when(databasePort.readScbAvailability("unknown")).thenReturn(List.of());
        assertTrue(service.getValues("unknown", ScbLevel.REGSO, null).isEmpty());
        verify(databasePort, never()).readScbValues(anyString(), any(), anyInt());
    }

}
