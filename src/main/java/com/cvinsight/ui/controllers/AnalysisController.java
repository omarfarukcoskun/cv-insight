package com.cvinsight.ui.controllers;

import com.cvinsight.model.CV;
import com.cvinsight.model.Feedback;
import com.cvinsight.model.Score;
import com.cvinsight.service.AnalysisObserver;
import com.cvinsight.service.AnalysisService;
import com.cvinsight.ui.SceneManager;
import com.cvinsight.ui.components.FeedbackPanel;
import com.cvinsight.ui.components.ScoreCard;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * PATTERN: Observer (subscriber side)
 *
 * Implements AnalysisObserver so AnalysisService can push progress events
 * directly into this controller. All UI updates are wrapped in Platform.runLater()
 * because the events arrive from a background virtual thread.
 */
public class AnalysisController implements AnalysisObserver {

    @FXML private VBox       loadingPane;
    @FXML private ProgressBar progressBar;
    @FXML private Label      statusLabel;

    @FXML private ScrollPane resultsPane;
    @FXML private VBox       scoreCardContainer;
    @FXML private Label      cvNameLabel;
    @FXML private Label      analyzedAtLabel;
    @FXML private VBox       feedbackContainer;
    @FXML private Button     reRunButton;

    @FXML private VBox       errorPane;
    @FXML private Label      errorMessageLabel;

    private CV              cv;
    private AnalysisService analysisService;

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm");

    // ── Called by UploadController after switching screens ───────────────────

    public void initWithCV(CV cv, AnalysisService analysisService) {
        this.cv              = cv;
        this.analysisService = analysisService;
        analysisService.addObserver(this);
        analysisService.analyze(cv);
    }

    // ── AnalysisObserver callbacks (called from background thread) ───────────

    @Override
    public void onProgressUpdate(int percent, String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(percent / 100.0);
            statusLabel.setText(message);
        });
    }

    @Override
    public void onAnalysisComplete(Score score, Feedback feedback) {
        Platform.runLater(() -> {
            showResults(score, feedback);
            analysisService.removeObserver(this);
        });
    }

    @Override
    public void onAnalysisError(String errorMessage) {
        Platform.runLater(() -> {
            loadingPane.setVisible(false);
            loadingPane.setManaged(false);
            errorMessageLabel.setText(errorMessage);
            errorPane.setVisible(true);
            errorPane.setManaged(true);
            analysisService.removeObserver(this);
        });
    }

    // ── UI update ────────────────────────────────────────────────────────────

    private void showResults(Score score, Feedback feedback) {
        // Swap loading → results
        loadingPane.setVisible(false);
        loadingPane.setManaged(false);
        resultsPane.setVisible(true);
        resultsPane.setManaged(true);
        reRunButton.setVisible(true);
        reRunButton.setManaged(true);

        // Score card
        ScoreCard scoreCard = new ScoreCard();
        scoreCard.setScore(score.getOverall());
        scoreCardContainer.getChildren().setAll(scoreCard);

        // Meta labels
        if (cv.getSourceFile() != null) {
            cvNameLabel.setText(Path.of(cv.getSourceFile()).getFileName().toString());
        }
        analyzedAtLabel.setText(score.getAnalyzedAt().format(DATE_FMT));

        // Feedback panel
        FeedbackPanel feedbackPanel = new FeedbackPanel();
        feedbackPanel.populate(feedback);
        feedbackContainer.getChildren().setAll(feedbackPanel);
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @FXML
    private void handleReRun() {
        // Reset to loading state and re-run (handles both results→retry and error→retry)
        loadingPane.setVisible(true);
        loadingPane.setManaged(true);
        resultsPane.setVisible(false);
        resultsPane.setManaged(false);
        errorPane.setVisible(false);
        errorPane.setManaged(false);
        reRunButton.setVisible(false);
        reRunButton.setManaged(false);
        progressBar.setProgress(0);
        statusLabel.setText("Preparing...");
        statusLabel.setStyle("");

        analysisService.addObserver(this);
        analysisService.analyze(cv);
    }

    @FXML private void handleBack()    { SceneManager.switchTo("dashboard.fxml"); }

    @FXML
    private void handleCompare() {
        SceneManager.switchTo("comparison.fxml");
        // Future: get loader.getController() and pass cv for pre-filtered comparison
    }
}
