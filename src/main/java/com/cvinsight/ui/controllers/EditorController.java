package com.cvinsight.ui.controllers;

import com.cvinsight.model.SectionType;
import com.cvinsight.ui.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * CV Editor — Week 7 stub.
 * Sections list and text area are wired; full save/analyze logic to be implemented.
 */
public class EditorController implements Initializable {

    @FXML private ListView<String> sectionList;
    @FXML private TextArea         editorArea;
    @FXML private Label            sectionTitleLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sectionList.setItems(FXCollections.observableArrayList(
            "Summary", "Experience", "Education", "Skills", "Projects"
        ));
        sectionList.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, selected) -> {
                if (selected != null) sectionTitleLabel.setText(selected);
            }
        );
    }

    @FXML private void handleAddSection() { /* TODO: prompt for section name */ }
    @FXML private void handleAnalyze()    { /* TODO: build CV from editor content, navigate to upload flow */ }
    @FXML private void handleBack()       { SceneManager.switchTo("dashboard.fxml"); }
}
