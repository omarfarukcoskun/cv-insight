package com.cvinsight.service;

import com.cvinsight.model.Feedback;
import com.cvinsight.model.Score;

/**
 * PATTERN: Observer (subscriber side)
 *
 * Dev B implements this interface in AnalysisController to receive
 * live updates during an async AI analysis run.
 *
 * Dev A fires events into it via AnalysisService — neither side
 * needs to know anything about the other's implementation.
 *
 * Usage (Dev B):
 *
 *   public class AnalysisController implements AnalysisObserver {
 *
 *       public void onProgressUpdate(int percent, String message) {
 *           progressBar.setProgress(percent / 100.0);
 *           statusLabel.setText(message);
 *       }
 *
 *       public void onAnalysisComplete(Score score, Feedback feedback) {
 *           scoreCard.setScore(score.getOverall());
 *           feedbackPanel.populate(feedback);
 *       }
 *
 *       public void onAnalysisError(String errorMessage) {
 *           showErrorDialog(errorMessage);
 *       }
 *   }
 */
public interface AnalysisObserver {

    /**
     * Called repeatedly during analysis to report progress.
     *
     * @param percent  0–100
     * @param message  Human-readable status, e.g. "Sending CV to AI..."
     */
    void onProgressUpdate(int percent, String message);

    /**
     * Called once when analysis finishes successfully.
     */
    void onAnalysisComplete(Score score, Feedback feedback);

    /**
     * Called if analysis fails at any point.
     */
    void onAnalysisError(String errorMessage);
}
