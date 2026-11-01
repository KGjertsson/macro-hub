package com.kg.macroanalyzer.regionalstatistics.application.port.in;

import com.kg.macroanalyzer.regionalstatistics.domain.ScbLevel;

import java.util.List;
import java.util.Optional;

public interface RegionalStatisticsQueries {

    List<RegionalIndicatorDto> getRegionalIndicators();

    Optional<RegionalValuesDto> getRegionalValues(String indicatorId, ScbLevel level, Integer year);

}
