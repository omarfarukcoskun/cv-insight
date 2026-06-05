package com.cvinsight.ui.controllers;

import com.cvinsight.cv.parser.CVParseException;
import com.cvinsight.db.dao.FeedbackDao;
import com.cvinsight.db.dao.ScoreDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.Score;
import com.cvinsight.service.AnalysisService;
import com.cvinsight.service.CVService;
import com.cvinsight.service.SessionManager;
import com.cvinsight.ui.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import com.cvinsight.ui.components.DeleteConfirmDialog;
import com.cvinsight.ui.components.PDFPreviewDialog;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.File;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label userAvatarLabel;
    @FXML private Label userNameLabel;
    @FXML private Label welcomeLabel;
    @FXML private HBox  statsRow;
    @FXML private HBox  actionsRow;
    @FXML private VBox  historyContainer;
    @FXML private VBox  tipsSection;
    @FXML private VBox  tipsContainer;

    private final CVService    cvService    = new CVService();
    private final ScoreDao     scoreDao     = new ScoreDao();
    private final FeedbackDao  feedbackDao  = new FeedbackDao();
    private List<CV> history;
    private List<Score> scores;

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String username = SessionManager.getInstance().getCurrentUser().getUsername();

        // Avatar initials (up to 2 chars)
        String initials = username.length() >= 2
            ? username.substring(0, 2).toUpperCase()
            : username.toUpperCase();
        userAvatarLabel.setText(initials);
        userNameLabel.setText(username);
        welcomeLabel.setText("Welcome back, " + username + "!");

        history = cvService.getHistory();
        scores  = collectScores();

        buildStatsRow();
        buildActionsRow();
        loadHistory();
        buildTipsSection();
    }

    // ── Stats ────────────────────────────────────────────────────────────────

    private List<Score> collectScores() {
        List<Score> result = new ArrayList<>();
        for (CV cv : history) {
            try {
                scoreDao.findByCvId(cv.getId()).ifPresent(result::add);
            } catch (SQLException ignored) {}
        }
        return result;
    }

    private void buildStatsRow() {
        int total    = history.size();
        int avgScore = scores.isEmpty() ? -1
                     : (int) scores.stream().mapToInt(Score::getOverall).average().orElse(0);
        int best     = scores.isEmpty() ? -1
                     : scores.stream().mapToInt(Score::getOverall).max().orElse(0);
        int done     = scores.size();

        statsRow.getChildren().addAll(
            statCard("Total Resumes",
                     String.valueOf(total),
                     avgScore >= 0 ? total + " uploaded" : "Get started",
                     "#6366f1", "#eef2ff",
                     "Click to view all resumes",
                     () -> SceneManager.switchTo("myresumes.fxml")),
            statCard("Average Score",
                     avgScore >= 0 ? avgScore + " / 100" : "—",
                     avgScore >= 0 ? scoreLabel(avgScore) : "No analyses yet",
                     "#0ea5e9", "#f0f9ff", null, null),
            statCard("Best Score",
                     best >= 0 ? best + " / 100" : "—",
                     best >= 0 ? "Personal best" : "Analyze a resume",
                     "#10b981", "#f0fdf4", null, null),
            statCard("Analyses Done",
                     String.valueOf(done),
                     done == 1 ? "1 analysis run" : done + " analyses run",
                     "#f59e0b", "#fffbeb", null, null)
        );
    }

    private VBox statCard(String title, String value, String sub,
                          String accentColor, String bgColor,
                          String hintText, Runnable onClick) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);

        // Animated dot → pill indicator
        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 4;");
        dot.setPrefWidth(4);
        dot.setMinWidth(0);
        dot.setMaxWidth(Double.MAX_VALUE);
        dot.setPrefHeight(4);
        dot.setMinHeight(4);
        dot.setMaxHeight(4);

        Timeline expand = new Timeline(new KeyFrame(Duration.millis(300),
            new KeyValue(dot.prefWidthProperty(), 24, Interpolator.EASE_BOTH)));
        Timeline shrink = new Timeline(new KeyFrame(Duration.millis(300),
            new KeyValue(dot.prefWidthProperty(), 4,  Interpolator.EASE_BOTH)));

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-label");

        Label subLabel = new Label(sub);
        subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + accentColor
            + "; -fx-background-color: " + bgColor
            + "; -fx-background-radius: 5; -fx-padding: 2 7 2 7;");

        // Hint label — always present on all cards to equalize card heights.
        // Non-clickable cards get empty text (same height, zero opacity forever).
        Label hintLabel = new Label(hintText != null ? hintText : "");
        hintLabel.getStyleClass().add("hint-label");
        hintLabel.setOpacity(0.0);

        card.getChildren().addAll(dot, valueLabel, titleLabel, subLabel, hintLabel);

        if (onClick != null) {
            card.getStyleClass().add("clickable-stat-card");
            card.setCursor(javafx.scene.Cursor.HAND);
            card.setOnMouseClicked(e -> onClick.run());

            FadeTransition showHint = new FadeTransition(Duration.millis(200), hintLabel);
            FadeTransition hideHint = new FadeTransition(Duration.millis(150), hintLabel);

            card.setOnMouseEntered(e -> {
                shrink.stop(); expand.play();
                hideHint.stop();
                showHint.setFromValue(hintLabel.getOpacity());
                showHint.setToValue(1.0);
                showHint.play();
            });
            card.setOnMouseExited(e -> {
                expand.stop(); shrink.play();
                showHint.stop();
                hideHint.setFromValue(hintLabel.getOpacity());
                hideHint.setToValue(0.0);
                hideHint.play();
            });
        } else {
            card.setOnMouseEntered(e -> { shrink.stop(); expand.play(); });
            card.setOnMouseExited(e  -> { expand.stop(); shrink.play(); });
        }

        return card;
    }

    private String scoreLabel(int avg) {
        if (avg >= 80) return "Excellent";
        if (avg >= 65) return "Good";
        if (avg >= 50) return "Needs work";
        return "Improve it";
    }

    // ── Action Cards ─────────────────────────────────────────────────────────

    private void buildActionsRow() {
        actionsRow.getChildren().addAll(
            actionCard("↑",  "Upload Resume",
                       "Analyze your existing PDF and get an AI score",
                       "#4f46e5", "#eef2ff", this::handleUpload),
            actionCard("+",  "Build Resume",
                       "Create a professional resume from scratch with live preview",
                       "#10b981", "#f0fdf4", this::handleCreateCV),
            actionCard("=",  "Browse Examples",
                       "Explore sample resumes and see what high scores look like",
                       "#f59e0b", "#fffbeb", this::handleExamples)
        );
    }

    private VBox actionCard(String icon, String title, String desc,
                            String iconColor, String iconBg, Runnable action) {
        VBox card = new VBox(14);
        card.getStyleClass().add("action-card");
        card.setPrefHeight(160);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);

        // Icon circle
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(46, 46);
        iconBox.setMaxSize(46, 46);
        iconBox.setStyle("-fx-background-color: " + iconBg
            + "; -fx-background-radius: 12;");
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
            + iconColor + ";");
        iconBox.getChildren().add(iconLabel);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("action-title");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("action-desc");
        descLabel.setWrapText(true);

        // Push arrow to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1;");

        card.getChildren().addAll(iconBox, titleLabel, descLabel, spacer, arrow);

        // Hover: lift shadow + tinted border
        final String defaultStyle = "";
        final String hoverStyle =
            "-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 24; "
            + "-fx-border-color: " + iconBg + "; -fx-border-width: 1.5; "
            + "-fx-border-radius: 14; -fx-cursor: hand; "
            + "-fx-effect: dropshadow(gaussian, " + toRgba(iconColor, 0.18) + ", 22, 0, 0, 6);";

        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            arrow.setStyle("-fx-font-size: 15px; -fx-text-fill: " + iconColor + ";");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(defaultStyle);
            arrow.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1;");
        });
        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    /** Convert a hex color to rgba(r,g,b,a) string for use in -fx-effect. */
    private String toRgba(String hex, double alpha) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return String.format("rgba(%d,%d,%d,%.2f)", r, g, b, alpha);
    }

    // ── History ──────────────────────────────────────────────────────────────

    private void loadHistory() {
        if (history.isEmpty()) {
            historyContainer.getChildren().add(buildEmptyState());
            return;
        }
        for (CV cv : history) {
            historyContainer.getChildren().add(buildHistoryRow(cv));
        }
    }

    private HBox buildHistoryRow(CV cv) {
        HBox row = new HBox(14);
        row.getStyleClass().add("history-row");
        row.setAlignment(Pos.CENTER_LEFT);

        // File icon badge
        StackPane fileIcon = new StackPane();
        fileIcon.setMinSize(40, 40);
        fileIcon.setMaxSize(40, 40);
        fileIcon.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10;");
        Label fileIco = new Label("PDF");
        fileIco.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; "
            + "-fx-text-fill: #64748b; -fx-alignment: center;");
        fileIcon.getChildren().add(fileIco);

        // Name + date
        VBox nameCol = new VBox(3);
        HBox.setHgrow(nameCol, Priority.ALWAYS);

        String displayName = cv.getSourceFile() != null
            ? java.nio.file.Path.of(cv.getSourceFile()).getFileName().toString()
            : cv.getOwnerName();
        Label nameLabel = new Label(displayName);
        nameLabel.getStyleClass().add("history-name");

        Label dateLabel = new Label(cv.getUploadedAt().format(DATE_FMT));
        dateLabel.getStyleClass().add("history-meta");

        nameCol.getChildren().addAll(nameLabel, dateLabel);

        // Score chip
        int scoreVal = -1;
        String scoreText = "Pending";
        try {
            Optional<Score> s = scoreDao.findByCvId(cv.getId());
            if (s.isPresent()) {
                scoreVal  = s.get().getOverall();
                scoreText = scoreVal + " / 100";
            }
        } catch (SQLException ignored) {}

        Label chip = new Label(scoreText);
        chip.getStyleClass().add("score-chip");
        chip.getStyleClass().add(
            scoreVal >= 75 ? "score-chip-high"
          : scoreVal >= 50 ? "score-chip-mid"
          : scoreVal >= 0  ? "score-chip-low"
          :                  "score-chip-pending");

        // Action buttons
        final CV cvRef = cv;

        Button reAnalyzeBtn = new Button("Re-analyze");
        reAnalyzeBtn.getStyleClass().add("small-action-btn");
        reAnalyzeBtn.setOnAction(e -> {
            FXMLLoader loader = SceneManager.switchTo("analysis.fxml");
            AnalysisController ctl = loader.getController();
            ctl.initWithCV(cvRef, new AnalysisService());
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("small-danger-btn");
        deleteBtn.setOnAction(e -> {
            Window owner = deleteBtn.getScene().getWindow();
            if (DeleteConfirmDialog.show(owner, displayName)) {
                cvService.deleteCV(cvRef.getId());
                historyContainer.getChildren().remove(row);
                if (historyContainer.getChildren().isEmpty()) {
                    historyContainer.getChildren().add(buildEmptyState());
                }
            }
        });

        HBox btns = new HBox(8);
        if (cv.getSourceFile() != null) {
            Button viewPdfBtn = new Button("View PDF");
            viewPdfBtn.getStyleClass().add("small-action-btn");
            viewPdfBtn.setOnAction(e -> PDFPreviewDialog.show(
                viewPdfBtn.getScene().getWindow(), cvRef.getSourceFile(), displayName));
            btns.getChildren().add(viewPdfBtn);
        }
        btns.getChildren().addAll(reAnalyzeBtn, deleteBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(fileIcon, nameCol, chip, btns);
        return row;
    }

    private VBox buildEmptyState() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 40 0 40 0;");

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(72, 72);
        iconCircle.setMaxSize(72, 72);
        iconCircle.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 36;");
        Label iconLabel = new Label("[ ]");
        iconLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #6366f1; -fx-font-weight: bold;");
        iconCircle.getChildren().add(iconLabel);

        Label title = new Label("No resumes yet");
        title.getStyleClass().add("empty-state-title");

        Label sub = new Label("Start building your professional resume in minutes.");
        sub.getStyleClass().add("empty-state-sub");
        sub.setWrapText(true);

        HBox ctas = new HBox(12);
        ctas.setAlignment(Pos.CENTER);

        Button createBtn = new Button("Build Resume");
        createBtn.getStyleClass().add("primary-button");
        createBtn.setOnAction(e -> handleCreateCV());

        Button uploadBtn = new Button("Upload Resume");
        uploadBtn.getStyleClass().add("secondary-button");
        uploadBtn.setOnAction(e -> handleUpload());

        ctas.getChildren().addAll(createBtn, uploadBtn);
        box.getChildren().addAll(iconCircle, title, sub, ctas);
        return box;
    }

    // ── AI Tips ──────────────────────────────────────────────────────────────

    private static final String[][] FALLBACK_TIPS = {
        { "HIGH", "Add measurable achievements",
          "Numbers make your resume stand out — include percentages, team sizes, and impact." },
        { "HIGH", "Tailor keywords to job descriptions",
          "ATS systems filter by keywords. Mirror the language of the job posting you're targeting." },
        { "MED",  "Use strong action verbs",
          "Start each bullet point with verbs like Led, Built, Reduced, or Launched." },
        { "MED",  "Strengthen your skills section",
          "Group skills by category (Languages, Frameworks, Tools) to improve readability." },
    };

    private void buildTipsSection() {
        List<String[]> tips = loadDynamicTips();
        if (tips.isEmpty()) {
            tips = List.of(FALLBACK_TIPS);
        }
        for (String[] tip : tips) {
            tipsContainer.getChildren().add(buildTipCard(tip[0], tip[1], tip[2]));
        }
        tipsSection.setVisible(true);
        tipsSection.setManaged(true);
    }

    private List<String[]> loadDynamicTips() {
        for (CV cv : history) {
            try {
                Optional<Score> scoreOpt = scoreDao.findByCvId(cv.getId());
                if (scoreOpt.isEmpty()) continue;
                Optional<com.cvinsight.model.Feedback> fbOpt = feedbackDao.findByCvId(cv.getId());
                if (fbOpt.isEmpty()) continue;
                return buildTipsFromFeedback(fbOpt.get(), scoreOpt.get().getOverall());
            } catch (SQLException ignored) {}
        }
        return List.of();
    }

    private List<String[]> buildTipsFromFeedback(com.cvinsight.model.Feedback feedback, int score) {
        List<String[]> tips = new ArrayList<>();
        String weakPriority = score < 60 ? "HIGH" : score < 80 ? "MED" : "LOW";
        String sugPriority  = score < 60 ? "HIGH" : score < 80 ? "HIGH" : "MED";

        for (String w : feedback.getWeaknesses()) {
            tips.add(makeTip(weakPriority, w));
            if (tips.size() == 2) break;
        }
        for (String s : feedback.getSuggestions()) {
            tips.add(makeTip(sugPriority, s));
            if (tips.size() == 4) break;
        }
        return tips;
    }

    private static String[] makeTip(String priority, String text) {
        String title;
        if (text.length() <= 48) {
            title = text;
        } else {
            int cut = text.lastIndexOf(' ', 48);
            title = (cut > 20 ? text.substring(0, cut) : text.substring(0, 48)) + "…";
        }
        return new String[]{ priority, title, text };
    }

    private HBox buildTipCard(String priority, String title, String detail) {
        HBox card = new HBox(14);
        card.getStyleClass().add("tip-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Priority badge
        Label badge = new Label(priority);
        badge.getStyleClass().add(
            "HIGH".equals(priority) ? "tip-badge-high" : "tip-badge-med");

        // Text block
        VBox textBlock = new VBox(3);
        HBox.setHgrow(textBlock, Priority.ALWAYS);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("tip-text");
        detailLabel.setWrapText(true);
        textBlock.getChildren().addAll(titleLabel, detailLabel);

        card.getChildren().addAll(badge, textBlock);
        return card;
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private void handleUpload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select your CV");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CV Files", "*.pdf", "*.txt"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        Stage stage = (Stage) statsRow.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            CV cv = cvService.loadFromFile(file);
            FXMLLoader loader = SceneManager.switchTo("analysis.fxml");
            AnalysisController ctl = loader.getController();
            ctl.initWithCV(cv, new AnalysisService());
        } catch (CVParseException e) {
            // TODO: surface error inline on dashboard if needed
        } catch (RuntimeException e) {
            // TODO: surface error inline on dashboard if needed
        }
    }
    private void handleCreateCV() { SceneManager.switchTo("buildresume.fxml"); }
    private void handleExamples() { SceneManager.switchTo("comparison.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.switchTo("login.fxml");
    }
}
