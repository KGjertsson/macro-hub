package com.kg.macroanalyzer.parliament.application.port.in;

import lombok.Builder;

@Builder
public record AssignmentDto(
        String type,
        String organCode,
        String organName,
        String role,
        String status,
        String from,
        String to,
        boolean current
) {
}
