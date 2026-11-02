package com.kg.macroanalyzer.parliament.adaptors.http;

import com.kg.macroanalyzer.parliament.application.port.in.CompositionDto;
import com.kg.macroanalyzer.parliament.application.port.in.ElectionResultDto;
import com.kg.macroanalyzer.parliament.application.port.in.MemberDetailDto;
import com.kg.macroanalyzer.parliament.application.port.in.MemberSummaryDto;
import com.kg.macroanalyzer.parliament.application.port.in.ParliamentQueries;
import com.kg.macroanalyzer.parliament.application.port.in.PartyDto;
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

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class ParliamentController {

    private final ParliamentQueries parliamentQueries;

    @Autowired
    public ParliamentController(ParliamentQueries parliamentQueries) {
        this.parliamentQueries = parliamentQueries;
    }

    @GetMapping("/parliament/parties")
    public ResponseEntity<List<PartyDto>> getParties() {
        log.info("Received GET macro-analyzer/parliament/parties");

        return listResponse(parliamentQueries.getParties());
    }

    @GetMapping("/parliament/elections")
    public ResponseEntity<List<ElectionResultDto>> getElections() {
        log.info("Received GET macro-analyzer/parliament/elections");

        return listResponse(parliamentQueries.getElections());
    }

    @GetMapping("/parliament/composition")
    public ResponseEntity<CompositionDto> getComposition() {
        log.info("Received GET macro-analyzer/parliament/composition");
        final var composition = parliamentQueries.getComposition();
        if (composition.parties().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(composition);
    }

    @GetMapping("/parliament/members")
    public ResponseEntity<List<MemberSummaryDto>> getMembersOfParliament(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/parliament/members?year=%s".formatted(year));

        return listResponse(parliamentQueries.getMembersOfParliament(year));
    }

    @GetMapping("/parliament/members/{id}")
    public ResponseEntity<MemberDetailDto> getMemberOfParliament(@PathVariable("id") String id) {
        log.info("Received GET macro-analyzer/parliament/members/%s".formatted(id));

        return parliamentQueries.getMemberOfParliament(id)
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
