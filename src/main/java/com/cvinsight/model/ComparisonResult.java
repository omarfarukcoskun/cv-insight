package com.cvinsight.model;

import java.util.List;

public record ComparisonResult(
    List<SectionComparison> sections,
    List<String>            overallTips,
    int                     userScore,
    int                     exampleScore
) {}
