package com.kg.macroanalyzer.election.adaptors.http;

import com.kg.macroanalyzer.election.application.port.in.ElectionQueries;
import com.kg.macroanalyzer.election.application.port.in.LocalElectionDetailDto;
import com.kg.macroanalyzer.election.application.port.in.LocalElectionSummaryDto;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class ElectionController {

    private final ElectionQueries electionQueries;

    @Autowired
    public ElectionController(ElectionQueries electionQueries) {
        this.electionQueries = electionQueries;
    }

    @GetMapping("/municipalities/elections")
    public ResponseEntity<List<LocalElectionSummaryDto>> getMunicipalityElections(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/municipalities/elections?year=%s".formatted(year));

        return listResponse(electionQueries.getLocalElectionResults(LocalElectionType.KF, year));
    }

    @GetMapping("/municipalities/{code}")
    public ResponseEntity<LocalElectionDetailDto> getMunicipality(
            @PathVariable("code") String code,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/municipalities/%s?year=%s".formatted(code, year));

        return detailResponse(electionQueries.getLocalElectionResult(LocalElectionType.KF, year, code));
    }

    @GetMapping("/regions/elections")
    public ResponseEntity<List<LocalElectionSummaryDto>> getRegionElections(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/regions/elections?year=%s".formatted(year));

        return listResponse(electionQueries.getLocalElectionResults(LocalElectionType.RF, year));
    }

    @GetMapping("/regions/{code}")
    public ResponseEntity<LocalElectionDetailDto> getRegion(
            @PathVariable("code") String code,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/regions/%s?year=%s".formatted(code, year));

        return detailResponse(electionQueries.getLocalElectionResult(LocalElectionType.RF, year, code));
    }

    @GetMapping("/constituencies/elections")
    public ResponseEntity<List<LocalElectionSummaryDto>> getConstituencyElections(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/constituencies/elections?year=%s".formatted(year));

        return listResponse(electionQueries.getLocalElectionResults(LocalElectionType.RD, year));
    }

    @GetMapping("/constituencies/{code}")
    public ResponseEntity<LocalElectionDetailDto> getConstituency(
            @PathVariable("code") String code,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/constituencies/%s?year=%s".formatted(code, year));

        return detailResponse(electionQueries.getLocalElectionResult(LocalElectionType.RD, year, code));
    }

    private static <T> ResponseEntity<T> detailResponse(Optional<T> detail) {
        return detail
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static <T> ResponseEntity<List<T>> listResponse(List<T> list) {
        final var responseCode = list.isEmpty()
                ? HttpStatus.NO_CONTENT
                : HttpStatus.OK;

        return new ResponseEntity<>(list, responseCode);
    }

}
