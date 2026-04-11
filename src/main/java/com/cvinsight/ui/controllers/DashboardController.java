package com.cvinsight.ui.controllers;

import com.cvinsight.db.dao.ScoreDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.Score;
import com.cvinsight.service.CVService;
import com.cvinsight.service.SessionManager;
import com.cvinsight.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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

        row.getChildren().addAll(nameLabel, scoreLabel, dateLabel);
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
