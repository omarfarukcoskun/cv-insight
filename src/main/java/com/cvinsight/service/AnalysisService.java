package com.cvinsight.service;

import com.cvinsight.ai.AnalysisResult;
import com.cvinsight.ai.facade.AIAnalysisFacade;
import com.cvinsight.db.dao.CVDao;
import com.cvinsight.db.dao.FeedbackDao;
import com.cvinsight.db.dao.ScoreDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.CVStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PATTERN: Observer (publisher side)
 *
 * Runs AI analysis asynchronously on a virtual thread and fires progress
 * events to all registered AnalysisObserver instances.
 *
 * Dev B registers AnalysisController as an observer before calling analyze().
 * Dev A's AI pipeline fires back through this service — neither side is coupled.
 *
 * Usage (Dev B):
 *
 *   analysisService.addObserver(analysisController);
 *   analysisService.analyze(cv);
 *   // controller receives onProgressUpdate(), onAnalysisComplete(), or onAnalysisError()
 */
public class AnalysisService {

    // CopyOnWriteArrayList: safe for observers added/removed while events are firing
    private final List<AnalysisObserver> observers = new CopyOnWriteArrayList<>();

    private final AIAnalysisFacade facade;
    private final ScoreDao         scoreDao;
    private final FeedbackDao      feedbackDao;
    private final CVDao            cvDao;

    public AnalysisService() {
        this.facade      = new AIAnalysisFacade();
        this.scoreDao    = new ScoreDao();
        this.feedbackDao = new FeedbackDao();
        this.cvDao       = new CVDao();
    }

    public void addObserver(AnalysisObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AnalysisObserver observer) {
        observers.remove(observer);
    }

    /**
     * Kicks off async analysis on a Java 21 virtual thread.
     * Returns immediately — results arrive via observer callbacks.
     */
    public void analyze(CV cv) {
        Thread.ofVirtual().start(() -> runAnalysis(cv));
    }

    private void runAnalysis(CV cv) {
        try {
            notifyProgress(10, "Preparing CV for analysis...");

            notifyProgress(30, "Sending CV to AI...");
            AnalysisResult result = facade.analyze(cv);

            notifyProgress(80, "Saving results...");
            scoreDao.insert(result.getScore());
            feedbackDao.insert(result.getFeedback());
            cvDao.updateStatus(cv.getId(), CVStatus.ANALYZED);

            notifyProgress(100, "Analysis complete.");
            notifyComplete(result);

        } catch (SQLException e) {
            notifyError("Failed to save analysis results: " + e.getMessage());
        } catch (Exception e) {
            notifyError("Analysis failed: " + e.getMessage());
            try {
                cvDao.updateStatus(cv.getId(), CVStatus.ERROR);
            } catch (SQLException ignored) {}
        }
    }

    private void notifyProgress(int percent, String message) {
        for (AnalysisObserver o : observers) {
            o.onProgressUpdate(percent, message);
        }
    }

    private void notifyComplete(AnalysisResult result) {
        for (AnalysisObserver o : observers) {
            o.onAnalysisComplete(result.getScore(), result.getFeedback());
        }
    }

    private void notifyError(String message) {
        for (AnalysisObserver o : observers) {
            o.onAnalysisError(message);
        }
    }
}
