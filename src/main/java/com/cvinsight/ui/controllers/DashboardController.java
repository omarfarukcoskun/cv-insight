package com.cvinsight.ui.controllers;

import com.cvinsight.db.dao.ScoreDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.Score;
import com.cvinsight.service.AnalysisService;
import com.cvinsight.service.CVService;
import com.cvinsight.service.SessionManager;
import com.cvinsight.ui.SceneManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label userLabel;
    @FXML private VBox  historyContainer;
    @FXML private Label emptyHistoryLabel;

    private final CVService  cvService  = new CVService();
    private final ScoreDao   scoreDao   = new ScoreDao();

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText("Hi, " + SessionManager.getInstance().getCurrentUser().getUsername());
        loadHistory();
    }

    private void loadHistory() {
        List<CV> history = cvService.getHistory();

        if (history.isEmpty()) {
            emptyHistoryLabel.setVisible(true);
            emptyHistoryLabel.setManaged(true);
            return;
        }

        for (CV cv : history) {
            historyContainer.getChildren().add(buildHistoryItem(cv));
        }
    }

    private HBox buildHistoryItem(CV cv) {
        HBox row = new HBox(16);
        row.getStyleClass().add("history-item");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // File name
        Label nameLabel = new Label(cv.getSourceFile() != null
            ? java.nio.file.Path.of(cv.getSourceFile()).getFileName().toString()
            : cv.getOwnerName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Score (fetch from DB)
        String scoreText = "Pending";
        try {
            Optional<Score> score = scoreDao.findByCvId(cv.getId());
            if (score.isPresent()) {
                scoreText = "Score: " + score.get().getOverall() + "/100";
            }
        } catch (SQLException ignored) {}
        Label scoreLabel = new Label(scoreText);
        scoreLabel.setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold;");

        // Date
        Label dateLabel = new Label(cv.getUploadedAt().format(DATE_FMT));
        dateLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

        // Re-analyze button
        Button reAnalyzeBtn = new Button("Re-analyze");
        reAnalyzeBtn.getStyleClass().add("secondary-button");
        reAnalyzeBtn.setTooltip(new Tooltip("Run AI analysis again on this CV"));
        reAnalyzeBtn.setOnAction(e -> {
            FXMLLoader loader = SceneManager.switchTo("analysis.fxml");
            AnalysisController controller = loader.getController();
            controller.initWithCV(cv, new AnalysisService());
        });

        // Delete button — outline by default, fades to solid red on hover
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");

        FadeTransition fadeIn  = new FadeTransition(Duration.millis(200), deleteBtn);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), deleteBtn);

        deleteBtn.setOnMouseEntered(e -> {
            fadeOut.stop();
            deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white;");
            deleteBtn.setOpacity(0.0);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        deleteBtn.setOnMouseExited(e -> {
            fadeIn.stop();
            fadeOut.setFromValue(deleteBtn.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                deleteBtn.setStyle("");
                deleteBtn.setOpacity(1.0);
            });
            fadeOut.play();
        });

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will permanently delete the CV and its analysis results.",
                ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Delete CV");
            confirm.setHeaderText("Are you sure?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cvService.deleteCV(cv.getId());
                    historyContainer.getChildren().remove(row);
                    if (historyContainer.getChildren().isEmpty()) {
                        emptyHistoryLabel.setVisible(true);
                        emptyHistoryLabel.setManaged(true);
                    }
                }
            });
        });

        row.getChildren().addAll(nameLabel, scoreLabel, dateLabel, reAnalyzeBtn, deleteBtn);
        return row;
    }

    @FXML private void handleUpload()    { SceneManager.switchTo("upload.fxml"); }
    @FXML private void handleCreateCV()  { SceneManager.switchTo("editor.fxml"); }
    @FXML private void handleExamples()  { SceneManager.switchTo("comparison.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.switchTo("login.fxml");
    }
}
