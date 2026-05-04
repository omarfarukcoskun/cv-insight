package com.cvinsight.comparison;

import com.cvinsight.model.CV;

/**
 * PATTERN: Strategy (context)
 *
 * Holds a ScoringStrategy and delegates comparison work to it.
 * The UI can swap the strategy at runtime (e.g. user picks "Technical" or "General"
 * from a dropdown) without touching any other class.
 *
 * Usage:
 *   ComparisonEngine engine = new ComparisonEngine(new TechnicalScoringStrategy());
 *   ComparisonResult result = engine.compare(userCV, exampleCV);
 *
 *   engine.setStrategy(new GeneralScoringStrategy()); // swap at runtime
 *   ComparisonResult result2 = engine.compare(userCV, exampleCV);
 */
public class ComparisonEngine {

    private ScoringStrategy strategy;

    public ComparisonEngine(ScoringStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ScoringStrategy strategy) {
        this.strategy = strategy;
    }

    public ScoringStrategy getStrategy() {
        return strategy;
    }

    /**
     * Compares userCV against exampleCV using the current strategy.
     *
     * @param userCV    the CV being evaluated
     * @param exampleCV a successful example from the database
     * @return          a ComparisonResult with score and metadata
     */
    public ComparisonResult compare(CV userCV, CV exampleCV) {
        int score = strategy.compare(userCV, exampleCV);
        return new ComparisonResult(
            exampleCV.getId(),
            exampleCV.getOwnerName(),
            score,
            strategy.getStrategyName()
        );
    }
}
