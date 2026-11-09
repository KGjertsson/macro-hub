package com.kg.macroanalyzer.auth.domain;

import lombok.Getter;

@Getter
public class InvalidInputException extends RuntimeException {

    private final String field;

    public InvalidInputException(String field) {
        super("Invalid " + field);
        this.field = field;
    }

}
