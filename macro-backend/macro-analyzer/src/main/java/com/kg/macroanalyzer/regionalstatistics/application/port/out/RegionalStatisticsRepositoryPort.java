package com.kg.macroanalyzer.regionalstatistics.application.port.out;

import com.kg.macroanalyzer.regionalstatistics.domain.GeoVersion;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbAvailability;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicator;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicatorResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RegionalStatisticsRepositoryPort {

    List<ScbIndicator> readScbIndicators();

    Map<String, Instant> readScbTableStamps();

    Set<String> readScbIndicatorsWithData();

    Integer writeScbIndicatorResult(ScbIndicatorResult result);

    void writeScbTableStamps(Map<String, Instant> stamps, Set<String> droppedTables);

    List<ScbAvailability> readScbAvailability();

    List<ScbAvailability> readScbAvailability(String indicatorId);

    Map<String, Double> readScbValues(String indicatorId, GeoVersion geoVersion, int year);

}
