package com.kg.macroanalyzer.application.ports.driving.out.parliament;

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
