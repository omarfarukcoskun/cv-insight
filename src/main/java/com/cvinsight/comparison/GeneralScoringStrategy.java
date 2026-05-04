package com.cvinsight.comparison;

import com.cvinsight.model.CV;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * PATTERN: Strategy (implementation 1)
 *
 * Treats all CV content equally — no section weighting.
 * Computes Jaccard similarity on the word sets of both CVs.
 *
 * Good for: general / non-technical roles where every section matters equally.
 */
public class GeneralScoringStrategy implements ScoringStrategy {

    private static final Set<String> STOP_WORDS = Set.of(
        "a","an","the","and","or","but","in","on","at","to","for","of","with",
        "is","are","was","were","be","been","have","has","had","do","does","did",
        "i","my","me","we","our","you","your","it","its","this","that","as","by"
    );

    @Override
    public int compare(CV userCV, CV exampleCV) {
        if (userCV.getRawText() == null || exampleCV.getRawText() == null) return 0;

        Set<String> userWords    = tokenize(userCV.getRawText());
        Set<String> exampleWords = tokenize(exampleCV.getRawText());

        if (userWords.isEmpty() || exampleWords.isEmpty()) return 0;

        // Jaccard similarity: |intersection| / |union|
        Set<String> intersection = new HashSet<>(userWords);
        intersection.retainAll(exampleWords);

        Set<String> union = new HashSet<>(userWords);
        union.addAll(exampleWords);

        double jaccard = (double) intersection.size() / union.size();
        return (int) Math.round(jaccard * 100);
    }

    @Override
    public String getStrategyName() { return "General"; }

    protected Set<String> tokenize(String text) {
        return new HashSet<>(
            Arrays.stream(text.toLowerCase().split("[^a-z0-9]+"))
                  .filter(w -> w.length() > 2 && !STOP_WORDS.contains(w))
                  .toList()
        );
    }
}
