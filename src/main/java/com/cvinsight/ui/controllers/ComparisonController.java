package com.cvinsight.ui.controllers;

import com.cvinsight.db.dao.ExampleCVDao;
import com.cvinsight.model.CV;
import com.cvinsight.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Browse successful CV examples — Week 6 stub.
 * Loads examples from DB and displays them; compare logic to be added.
 */
public class ComparisonController implements Initializable {

    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> companyFilter;
    @FXML private VBox             examplesContainer;
    @FXML private Label            emptyLabel;

    private final ExampleCVDao exampleCVDao = new ExampleCVDao();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadExamples(exampleCVDao::findAll);
    }

    @FXML
    private void handleFilter() {
        String category = categoryFilter.getValue();
        String company  = companyFilter.getValue();
        if (category != null && !category.isBlank()) {
            loadExamples(() -> exampleCVDao.findByCategory(category));
        } else if (company != null && !company.isBlank()) {
            loadExamples(() -> exampleCVDao.findByCompany(company));
        } else {
            loadExamples(exampleCVDao::findAll);
        }
    }

    @FXML
    private void handleClear() {
        categoryFilter.setValue(null);
        companyFilter.setValue(null);
        loadExamples(exampleCVDao::findAll);
    }

    private void loadExamples(ExampleSupplier supplier) {
        examplesContainer.getChildren().clear();
        try {
            List<CV> examples = supplier.get();
            if (examples.isEmpty()) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
            } else {
                emptyLabel.setVisible(false);
                emptyLabel.setManaged(false);
                for (CV cv : examples) {
                    examplesContainer.getChildren().add(buildExampleCard(cv));
                }
            }
        } catch (SQLException e) {
            emptyLabel.setText("Failed to load examples: " + e.getMessage());
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }
    }

    private HBox buildExampleCard(CV cv) {
        HBox card = new HBox(16);
        card.getStyleClass().add("history-item");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(cv.getOwnerName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label viewBtn    = new Label("[ View ]");
        Label compareBtn = new Label("[ Compare ]");
        viewBtn.setStyle("-fx-text-fill: #4f46e5; -fx-cursor: hand;");
        compareBtn.setStyle("-fx-text-fill: #4f46e5; -fx-cursor: hand;");

        card.getChildren().addAll(nameLabel, viewBtn, compareBtn);
        return card;
    }

    @FXML private void handleBack() { SceneManager.switchTo("dashboard.fxml"); }

    @FunctionalInterface
    private interface ExampleSupplier {
        List<CV> get() throws SQLException;
    }
}
