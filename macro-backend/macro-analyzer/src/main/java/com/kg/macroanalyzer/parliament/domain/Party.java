package com.kg.macroanalyzer.parliament.domain;

import lombok.Builder;

@Builder
public record Party(String code, String name, String color, String ideology, boolean active) {
}
