package com.cvinsight.ui.controllers;

import com.cvinsight.cv.parser.CVParseException;
import com.cvinsight.model.CV;
import com.cvinsight.service.AnalysisService;
import com.cvinsight.service.CVService;
import com.cvinsight.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class UploadController {

    @FXML private VBox   dropZone;
    @FXML private Label  selectedFileLabel;
    @FXML private Label  errorLabel;
    @FXML private Button analyzeButton;

    private final CVService       cvService       = new CVService();
    private final AnalysisService analysisService = new AnalysisService();

    private File selectedFile;

    // ── Drag and Drop ────────────────────────────────────────────────────────

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
            dropZone.getStyleClass().add("drop-zone-active");
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() && !db.getFiles().isEmpty()) {
            setSelectedFile(db.getFiles().get(0));
            event.setDropCompleted(true);
        }
        dropZone.getStyleClass().remove("drop-zone-active");
        event.consume();
    }

    @FXML
    private void handleDragExited(DragEvent event) {
        dropZone.getStyleClass().remove("drop-zone-active");
        event.consume();
    }

    // ── File Browser ─────────────────────────────────────────────────────────

    @FXML
    private void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select your CV");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CV Files", "*.pdf", "*.txt"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = chooser.showOpenDialog(SceneManager.getStage());
        if (file != null) {
            setSelectedFile(file);
        }
    }

    private void setSelectedFile(File file) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".pdf") && !name.endsWith(".txt")) {
            showError("Unsupported file type. Please upload a PDF or TXT file.");
            return;
        }
        selectedFile = file;
        selectedFileLabel.setText("Selected: " + file.getName() + " ✓");
        selectedFileLabel.setVisible(true);
        selectedFileLabel.setManaged(true);
        analyzeButton.setVisible(true);
        analyzeButton.setManaged(true);
        clearError();
    }

    // ── Analyze ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAnalyze() {
        if (selectedFile == null) {
            showError("Please select a file first.");
            return;
        }

        try {
            CV cv = cvService.loadFromFile(selectedFile);

            // Switch to analysis screen and hand it the CV + service
            FXMLLoader loader = SceneManager.switchTo("analysis.fxml");
            AnalysisController controller = loader.getController();
            controller.initWithCV(cv, analysisService);

        } catch (CVParseException e) {
            showError("Could not read file: " + e.getMessage());
        } catch (RuntimeException e) {
            showError("Unexpected error: " + e.getMessage());
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @FXML private void handleBack() { SceneManager.switchTo("dashboard.fxml"); }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
