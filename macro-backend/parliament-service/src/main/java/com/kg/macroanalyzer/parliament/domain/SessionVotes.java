package com.kg.macroanalyzer.parliament.domain;

// Votes cast during one riksmöte (parliamentary year, e.g. "2025/26")
public record SessionVotes(String riksmote, int yes, int no, int absent, int abstain) {
}
