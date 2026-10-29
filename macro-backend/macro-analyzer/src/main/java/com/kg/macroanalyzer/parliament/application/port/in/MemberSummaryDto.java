package com.kg.macroanalyzer.parliament.application.port.in;

import lombok.Builder;

@Builder
public record MemberSummaryDto(
        String id,
        String name,
        String party,
        String constituency,
        String status,
        String imageUrl,
        String currentRole,
        double yearsInParliament,
        Double attendancePct
) {
}
