package com.cvinsight.model;

public record SectionComparison(
    String name,
    String userContent,
    String exampleContent,
    String verdict,   // "better" | "equal" | "worse"
    String tip        // non-empty only when verdict == "worse"
) {}
