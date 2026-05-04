package com.cvinsight.comparison;

/**
 * Holds the result of comparing a user's CV against one example CV.
 */
public class ComparisonResult {

    private final String exampleCVId;
    private final String exampleTitle;   // e.g. "Google — Software Eng. Intern"
    private final int    similarityScore; // 0–100
    private final String strategyUsed;

    public ComparisonResult(String exampleCVId, String exampleTitle,
                            int similarityScore, String strategyUsed) {
        this.exampleCVId     = exampleCVId;
        this.exampleTitle    = exampleTitle;
        this.similarityScore = similarityScore;
        this.strategyUsed    = strategyUsed;
    }

    public String getExampleCVId()     { return exampleCVId; }
    public String getExampleTitle()    { return exampleTitle; }
    public int    getSimilarityScore() { return similarityScore; }
    public String getStrategyUsed()    { return strategyUsed; }
}
