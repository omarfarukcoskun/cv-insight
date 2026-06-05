package com.cvinsight.ui.controllers;

import com.cvinsight.db.dao.ScoreDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.Score;
import com.cvinsight.service.AnalysisService;
import com.cvinsight.service.CVService;
import com.cvinsight.ui.SceneManager;
import com.cvinsight.ui.components.DeleteConfirmDialog;
import com.cvinsight.ui.components.PDFPreviewDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyResumesController implements Initializable {

    @FXML private Label countLabel;
    @FXML private HBox  errorContainer;
    @FXML private Label errorLabel;
    @FXML private VBox  listContainer;

    private final CVService cvService = new CVService();
    private final ScoreDao  scoreDao  = new ScoreDao();

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadResumes();
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadResumes() {
        try {
            List<CV> resumes = cvService.getHistory();
            updateCountBadge(resumes.size());
            listContainer.getChildren().clear();

            if (resumes.isEmpty()) {
                listContainer.getChildren().add(buildEmptyState());
                return;
            }
            for (CV cv : resumes) {
                listContainer.getChildren().add(buildResumeRow(cv));
            }
        } catch (Exception ex) {
            showError("Could not load resumes: " + ex.getMessage());
        }
    }

    private void updateCountBadge(int count) {
        countLabel.setText(count + " resume" + (count != 1 ? "s" : ""));
    }

    // ── Resume row ────────────────────────────────────────────────────────────

    private HBox buildResumeRow(CV cv) {
        HBox row = new HBox(16);
        row.getStyleClass().add("resume-card");
        row.setAlignment(Pos.CENTER_LEFT);

        // Icon badge — purple for uploaded PDFs, green for app-built resumes
        boolean hasFile   = cv.getSourceFile() != null;
        String  iconBg    = hasFile ? "#eef2ff" : "#f0fdf4";
        String  iconColor = hasFile ? "#6366f1" : "#10b981";
        String  iconText  = hasFile ? "PDF"     : "CV";

        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(48, 48);
        iconBadge.setMaxSize(48, 48);
        iconBadge.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 12;");
        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + iconColor + ";");
        iconBadge.getChildren().add(iconLabel);

        // Name + date column
        VBox infoCol = new VBox(4);
        HBox.setHgrow(infoCol, Priority.ALWAYS);

        String displayName = hasFile
            ? java.nio.file.Path.of(cv.getSourceFile()).getFileName().toString()
            : cv.getOwnerName();
        Label nameLabel = new Label(displayName);
        nameLabel.getStyleClass().add("resume-name");

        Label dateLabel = new Label("Uploaded " + cv.getUploadedAt().format(DATE_FMT));
        dateLabel.getStyleClass().add("resume-meta");

        infoCol.getChildren().addAll(nameLabel, dateLabel);

        // Score + status column
        int     scoreVal   = -1;
        String  scoreText  = "Pending";
        boolean isAnalyzed = false;
        try {
            Optional<Score> s = scoreDao.findByCvId(cv.getId());
            if (s.isPresent()) {
                scoreVal   = s.get().getOverall();
                scoreText  = scoreVal + " / 100";
                isAnalyzed = true;
            }
        } catch (SQLException ignored) {}

        Label scoreChip = new Label(scoreText);
        scoreChip.getStyleClass().add("score-chip");
        scoreChip.getStyleClass().add(
            scoreVal >= 75 ? "score-chip-high"
          : scoreVal >= 50 ? "score-chip-mid"
          : scoreVal >= 0  ? "score-chip-low"
          :                  "score-chip-pending");

        Label statusChip = new Label(isAnalyzed ? "Analyzed" : "Not Analyzed");
        statusChip.getStyleClass().add("status-chip");
        statusChip.getStyleClass().add(isAnalyzed ? "status-analyzed" : "status-pending");

        VBox statusCol = new VBox(6, scoreChip, statusChip);
        statusCol.setAlignment(Pos.CENTER);
        statusCol.setMinWidth(110);

        // Action buttons
        final CV   cvRef  = cv;
        final HBox rowRef = row;

        Button showAnalysisBtn = new Button("Show Analysis");
        showAnalysisBtn.getStyleClass().add("small-action-btn");
        showAnalysisBtn.setTooltip(new Tooltip("Open AI analysis for this resume"));
        showAnalysisBtn.setOnAction(e -> navigateToAnalysis(cvRef));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("small-danger-btn");
        deleteBtn.setOnAction(e -> {
            if (DeleteConfirmDialog.show(deleteBtn.getScene().getWindow(), displayName)) {
                cvService.deleteCV(cvRef.getId());
                listContainer.getChildren().remove(rowRef);
                long remaining = listContainer.getChildren().stream()
                    .filter(n -> n instanceof HBox).count();
                if (remaining == 0) {
                    listContainer.getChildren().add(buildEmptyState());
                }
                updateCountBadge((int) remaining);
            }
        });

        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);
        if (hasFile) {
            Button viewPdfBtn = new Button("View PDF");
            viewPdfBtn.getStyleClass().add("small-action-btn");
            viewPdfBtn.setOnAction(e -> PDFPreviewDialog.show(
                viewPdfBtn.getScene().getWindow(), cvRef.getSourceFile(), displayName));
            btns.getChildren().add(viewPdfBtn);
        }
        btns.getChildren().addAll(showAnalysisBtn, deleteBtn);

        row.getChildren().addAll(iconBadge, infoCol, statusCol, btns);
        return row;
    }

    private void navigateToAnalysis(CV cv) {
        FXMLLoader loader = SceneManager.switchTo("analysis.fxml");
        AnalysisController ctl = loader.getController();
        ctl.initWithCV(cv, new AnalysisService());
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    private VBox buildEmptyState() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 60 0 60 0;");

        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(80, 80);
        iconCircle.setMaxSize(80, 80);
        iconCircle.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 40;");
        Label iconLabel = new Label("[ ]");
        iconLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: #6366f1; -fx-font-weight: bold;");
        iconCircle.getChildren().add(iconLabel);

        Label title = new Label("No resumes yet");
        title.getStyleClass().add("empty-state-title");

        Label sub = new Label("Upload a PDF resume or build one from scratch.");
        sub.getStyleClass().add("empty-state-sub");

        HBox ctas = new HBox(12);
        ctas.setAlignment(Pos.CENTER);

        Button uploadBtn = new Button("Upload Resume");
        uploadBtn.getStyleClass().add("primary-button");
        uploadBtn.setOnAction(e -> SceneManager.switchTo("upload.fxml"));

        Button buildBtn = new Button("Build Resume");
        buildBtn.getStyleClass().add("secondary-button");
        buildBtn.setOnAction(e -> SceneManager.switchTo("buildresume.fxml"));

        ctas.getChildren().addAll(uploadBtn, buildBtn);
        box.getChildren().addAll(iconCircle, title, sub, ctas);
        return box;
    }

    // ── Error state ───────────────────────────────────────────────────────────

    private void showError(String message) {
        errorLabel.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
        countLabel.setText("—");
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleBack()      { SceneManager.switchTo("dashboard.fxml"); }
    @FXML private void handleUploadNew() { SceneManager.switchTo("upload.fxml"); }

    @FXML
    private void handleRetry() {
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
        loadResumes();
    }
}
