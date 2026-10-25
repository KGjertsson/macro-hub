package com.kg.macroanalyzer.application.ports.driving.out.parliament;

import lombok.Builder;

@Builder
public record ContactDto(String email, String phone, String website) {
}
