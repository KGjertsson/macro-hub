package com.kg.macroanalyzer.regionalstatistics.adaptors.postgres;

import com.kg.macroanalyzer.regionalstatistics.application.port.out.RegionalStatisticsRepositoryPort;
import com.kg.macroanalyzer.regionalstatistics.domain.GeoVersion;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbAvailability;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicator;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbIndicatorResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RegionalStatisticsPostgresAdaptor implements RegionalStatisticsRepositoryPort {

    private final ScbRegionalRepository scbRegionalRepository;

    public RegionalStatisticsPostgresAdaptor(ScbRegionalRepository scbRegionalRepository) {
        this.scbRegionalRepository = scbRegionalRepository;
    }

    @Override
    public List<ScbIndicator> readScbIndicators() {
        return scbRegionalRepository.readIndicators();
    }

    @Override
    public Map<String, Instant> readScbTableStamps() {
        return scbRegionalRepository.readTableStamps();
    }

    @Override
    public Set<String> readScbIndicatorsWithData() {
        return scbRegionalRepository.readIndicatorsWithData();
    }

    @Override
    public Integer writeScbIndicatorResult(ScbIndicatorResult result) {
        return scbRegionalRepository.writeIndicatorResult(result);
    }

    @Override
    public void writeScbTableStamps(Map<String, Instant> stamps, Set<String> droppedTables) {
        scbRegionalRepository.writeTableStamps(stamps, droppedTables);
    }

    @Override
    public List<ScbAvailability> readScbAvailability() {
        return scbRegionalRepository.readAvailability();
    }

    @Override
    public List<ScbAvailability> readScbAvailability(String indicatorId) {
        return scbRegionalRepository.readAvailability(indicatorId);
    }

    @Override
    public Map<String, Double> readScbValues(String indicatorId, GeoVersion geoVersion, int year) {
        return scbRegionalRepository.readValues(indicatorId, geoVersion, year);
    }

}
