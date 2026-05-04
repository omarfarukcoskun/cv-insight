package com.cvinsight.ui.controllers;

import com.cvinsight.comparison.ComparisonResult;
import com.cvinsight.model.CV;
import com.cvinsight.service.ComparisonService;
import com.cvinsight.service.CVService;
import com.cvinsight.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ComparisonController implements Initializable {

    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> companyFilter;
    @FXML private VBox             examplesContainer;
    @FXML private Label            emptyLabel;

    private final ComparisonService comparisonService = new ComparisonService();
    private final CVService         cvService         = new CVService();
    private final com.cvinsight.db.dao.ExampleCVDao exampleCVDao = new com.cvinsight.db.dao.ExampleCVDao();

    // The user's most recently analysed CV — used for the Compare feature.
    // Null if the user has no analysis history yet.
    private CV userCV;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        populateFilterOptions();
        resolveUserCV();
        loadExamples(comparisonService.getAllExamples());
    }

    // ── Filter handlers ──────────────────────────────────────────────────────

    @FXML
    private void handleFilter() {
        String category = categoryFilter.getValue();
        String company  = companyFilter.getValue();

        List<CV> filtered;
        if (category != null && !category.isBlank()) {
            filtered = comparisonService.getExamplesByCategory(category);
        } else if (company != null && !company.isBlank()) {
            filtered = comparisonService.getExamplesByCompany(company);
        } else {
            filtered = comparisonService.getAllExamples();
        }
        loadExamples(filtered);
    }

    @FXML
    private void handleClear() {
        categoryFilter.setValue(null);
        companyFilter.setValue(null);
        loadExamples(comparisonService.getAllExamples());
    }

    // ── Display ──────────────────────────────────────────────────────────────

    private void loadExamples(List<CV> examples) {
        examplesContainer.getChildren().clear();

        if (examples.isEmpty()) {
            emptyLabel.setText("No examples found.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        // If we have the user's CV, rank examples by similarity score
        if (userCV != null) {
            List<ComparisonResult> ranked = comparisonService.compareAll(userCV);
            for (ComparisonResult result : ranked) {
                examples.stream()
                    .filter(cv -> cv.getId().equals(result.getExampleCVId()))
                    .findFirst()
                    .ifPresent(cv -> examplesContainer.getChildren()
                        .add(buildExampleCard(cv, result.getSimilarityScore())));
            }
        } else {
            for (CV cv : examples) {
                examplesContainer.getChildren().add(buildExampleCard(cv, -1));
            }
        }
    }

    private HBox buildExampleCard(CV cv, int similarityScore) {
        HBox card = new HBox(16);
        card.getStyleClass().add("history-item");
        card.setAlignment(Pos.CENTER_LEFT);

        // Title
        Label nameLabel = new Label(cv.getOwnerName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Similarity score badge (shown only when user has a CV)
        if (similarityScore >= 0) {
            String colour = similarityScore >= 60 ? "#16a34a" : "#d97706";
            Label matchLabel = new Label("Match: " + similarityScore + "%");
            matchLabel.setStyle("-fx-text-fill: " + colour + "; -fx-font-weight: bold;");
            card.getChildren().add(matchLabel);
        }

        // View button — shows raw CV text in a popup
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("secondary-button");
        viewBtn.setOnAction(e -> showViewDialog(cv));

        // Compare button — shows side-by-side score breakdown
        Button compareBtn = new Button("Compare");
        compareBtn.getStyleClass().add("primary-button");
        compareBtn.setDisable(userCV == null);
        if (userCV == null) {
            compareBtn.setTooltip(new Tooltip("Analyse a CV first to enable comparison"));
        }
        compareBtn.setOnAction(e -> showCompareDialog(cv, similarityScore));

        card.getChildren().addAll(nameLabel, viewBtn, compareBtn);
        return card;
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private void showViewDialog(CV exampleCV) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(exampleCV.getOwnerName());
        dialog.setWidth(640);
        dialog.setHeight(500);

        TextArea textArea = new TextArea(exampleCV.getRawText());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        VBox layout = new VBox(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        layout.setStyle("-fx-padding: 16;");

        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    private void showCompareDialog(CV exampleCV, int score) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Comparison — " + exampleCV.getOwnerName());
        dialog.setWidth(560);
        dialog.setHeight(340);

        String scoreColour = score >= 60 ? "#16a34a" : "#d97706";
        String strategyName = comparisonService.getCurrentStrategyName();

        Label headline = new Label("Similarity Score: " + score + "%");
        headline.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + scoreColour + ";");

        Label stratLabel = new Label("Strategy used: " + strategyName);
        stratLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        Label tip = new Label(score >= 60
            ? "Your CV aligns well with this example. Study the specific wording used."
            : "Your CV has low overlap. Consider adopting similar keywords and structure.");
        tip.setWrapText(true);
        tip.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Button switchBtn = new Button("Switch to " +
            (strategyName.equals("Technical") ? "General" : "Technical") + " Strategy");
        switchBtn.getStyleClass().add("secondary-button");
        switchBtn.setOnAction(e -> {
            comparisonService.useStrategy(
                strategyName.equals("Technical") ? "General" : "Technical"
            );
            dialog.close();
            showCompareDialog(exampleCV, comparisonService
                .getTopMatches(userCV, Integer.MAX_VALUE).stream()
                .filter(r -> r.getExampleCVId().equals(exampleCV.getId()))
                .mapToInt(ComparisonResult::getSimilarityScore)
                .findFirst().orElse(score));
        });

        VBox layout = new VBox(16, headline, stratLabel, tip, switchBtn);
        layout.setStyle("-fx-padding: 32; -fx-background-color: white;");
        layout.setAlignment(Pos.TOP_LEFT);

        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void resolveUserCV() {
        List<CV> history = cvService.getHistory();
        if (!history.isEmpty()) {
            userCV = history.get(0); // most recent first
        }
    }

    private void populateFilterOptions() {
        categoryFilter.getItems().setAll("Tech", "PM", "Design", "Finance", "Other");
        try {
            List<String> companies = exampleCVDao.findDistinctCompanies();
            companyFilter.getItems().setAll(companies);
        } catch (java.sql.SQLException e) {
            // non-critical — filter stays empty if DB read fails
        }
    }

    @FXML private void handleBack() { SceneManager.switchTo("dashboard.fxml"); }
}
