package com.cvinsight.comparison;

import com.cvinsight.model.CV;

/**
 * PATTERN: Strategy
 *
 * Defines the algorithm for computing a similarity score between two CVs.
 * The ComparisonEngine holds a ScoringStrategy and delegates to it, so
 * the algorithm can be swapped without touching the engine or the UI.
 *
 * Current implementations:
 *   TechnicalScoringStrategy — weights Skills and Projects sections heavily
 *   GeneralScoringStrategy   — treats all sections equally
 *
 * Adding a new strategy (e.g. AcademicScoringStrategy) = new class, no other changes.
 */
public interface ScoringStrategy {

    /**
     * Computes a similarity score between a user's CV and a successful example CV.
     *
     * @param userCV    the CV being evaluated
     * @param exampleCV a successful example from the database
     * @return          integer 0–100 representing similarity
     */
    int compare(CV userCV, CV exampleCV);

    /** Human-readable name shown in the UI. */
    String getStrategyName();
}
