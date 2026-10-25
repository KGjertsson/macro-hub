package com.kg.macroanalyzer.application.domain.parliament;

public record VoteTotals(int yes, int no, int absent, int abstain) {

    public static final VoteTotals NONE = new VoteTotals(0, 0, 0, 0);

}
