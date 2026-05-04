package com.cvinsight.ai;

import com.cvinsight.model.Feedback;
import com.cvinsight.model.Score;

/**
 * Container returned by AIAnalysisFacade.analyze().
 * Carries both the Score and Feedback produced from a single Claude API call.
 */
public class AnalysisResult {

    private final Score score;
    private final Feedback feedback;

    public AnalysisResult(Score score, Feedback feedback) {
        this.score = score;
        this.feedback = feedback;
    }

    public Score getScore() { return score; }
    public Feedback getFeedback() { return feedback; }
}
