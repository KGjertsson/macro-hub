package com.kg.macroanalyzer.regionalstatistics.adaptors.http;

import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalIndicatorDto;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalStatisticsQueries;
import com.kg.macroanalyzer.regionalstatistics.application.port.in.RegionalValuesDto;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class RegionalStatisticsController {

    private final RegionalStatisticsQueries regionalStatisticsQueries;

    @Autowired
    public RegionalStatisticsController(RegionalStatisticsQueries regionalStatisticsQueries) {
        this.regionalStatisticsQueries = regionalStatisticsQueries;
    }

    @GetMapping("/regional-statistics/indicators")
    public ResponseEntity<List<RegionalIndicatorDto>> getRegionalIndicators() {
        log.info("Received GET macro-analyzer/regional-statistics/indicators");

        return ResponseEntity.ok(regionalStatisticsQueries.getRegionalIndicators());
    }

    @GetMapping("/regional-statistics/values")
    public ResponseEntity<RegionalValuesDto> getRegionalValues(
            @RequestParam(name = "indicator") String indicator,
            @RequestParam(name = "level") String level,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/regional-statistics/values?indicator=%s&level=%s&year=%s"
                .formatted(indicator, level, year));
        final var scbLevel = ScbLevel.fromLabel(level);
        if (scbLevel.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return regionalStatisticsQueries.getRegionalValues(indicator, scbLevel.get(), year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

}
