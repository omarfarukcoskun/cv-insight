package com.cvinsight.ui.controllers;

import com.cvinsight.model.CV;
import com.cvinsight.model.CVSection;
import com.cvinsight.model.SectionType;
import com.cvinsight.service.AnalysisService;
import com.cvinsight.service.CVService;
import com.cvinsight.ui.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.*;

/**
 * CV Editor — allows users to build a CV section-by-section without uploading a file.
 *
 * State: sectionContents maps section name → text typed so far.
 * On "Analyze": builds List<CVSection>, calls CVService.saveNewCV(), passes to AnalysisController.
 */
public class EditorController implements Initializable {

    @FXML private ListView<String> sectionList;
    @FXML private TextArea         editorArea;
    @FXML private Label            sectionTitleLabel;
    @FXML private RadioButton      modernTemplate;
    @FXML private RadioButton      classicTemplate;
    @FXML private RadioButton      minimalTemplate;

    private final CVService cvService = new CVService();

    /** In-memory store: section name → typed content. Insertion-ordered. */
    private final Map<String, String> sectionContents = new LinkedHashMap<>();

    /** The section name currently loaded into editorArea. */
    private String currentSection = null;

    // Template definitions
    private static final List<String> TEMPLATE_MODERN  =
        List.of("Summary", "Experience", "Skills", "Projects", "Education");
    private static final List<String> TEMPLATE_CLASSIC =
        List.of("Summary", "Experience", "Education", "Skills");
    private static final List<String> TEMPLATE_MINIMAL =
        List.of("Summary", "Skills", "Experience");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Default to Classic template sections
        applyTemplate(TEMPLATE_CLASSIC);

        // When selection changes: save current content, load selected section
        sectionList.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, selected) -> {
                if (old != null) {
                    sectionContents.put(old, editorArea.getText());
                }
                if (selected != null) {
                    currentSection = selected;
                    sectionTitleLabel.setText(selected);
                    editorArea.setText(sectionContents.getOrDefault(selected, ""));
                    editorArea.setDisable(false);
                } else {
                    sectionTitleLabel.setText("Select a section to edit");
                    editorArea.clear();
                    editorArea.setDisable(true);
                    currentSection = null;
                }
            }
        );

        // Template radio buttons
        modernTemplate.setOnAction(e  -> applyTemplate(TEMPLATE_MODERN));
        classicTemplate.setOnAction(e -> applyTemplate(TEMPLATE_CLASSIC));
        minimalTemplate.setOnAction(e -> applyTemplate(TEMPLATE_MINIMAL));

        // Select first section by default
        if (!sectionList.getItems().isEmpty()) {
            sectionList.getSelectionModel().selectFirst();
        }
    }

    // ── Template ─────────────────────────────────────────────────────────────

    private void applyTemplate(List<String> sections) {
        // Preserve any content already typed for overlapping section names
        Map<String, String> preserved = new LinkedHashMap<>();
        for (String s : sections) {
            preserved.put(s, sectionContents.getOrDefault(s, ""));
        }
        sectionContents.clear();
        sectionContents.putAll(preserved);

        ObservableList<String> items = FXCollections.observableArrayList(sections);
        sectionList.setItems(items);

        // Reset editor area
        currentSection = null;
        editorArea.clear();
        editorArea.setDisable(true);
        sectionTitleLabel.setText("Select a section to edit");

        sectionList.getSelectionModel().selectFirst();
    }

    // ── Add Section ───────────────────────────────────────────────────────────

    @FXML
    private void handleAddSection() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Section");
        dialog.setHeaderText(null);
        dialog.setContentText("Section name:");

        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) return;
            if (sectionContents.containsKey(trimmed)) {
                // Section already exists — just select it
                sectionList.getSelectionModel().select(trimmed);
                return;
            }
            sectionContents.put(trimmed, "");
            sectionList.getItems().add(trimmed);
            sectionList.getSelectionModel().select(trimmed);
        });
    }

    // ── Analyze ───────────────────────────────────────────────────────────────

    @FXML
    private void handleAnalyze() {
        // Flush current editor content before building sections
        if (currentSection != null) {
            sectionContents.put(currentSection, editorArea.getText());
        }

        // Require at least one section with content
        boolean hasContent = sectionContents.values().stream()
            .anyMatch(c -> !c.isBlank());
        if (!hasContent) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                "Please add some content to at least one section before analyzing.",
                ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        List<CVSection> sections = buildSections();
        CV cv = cvService.saveNewCV(sections);

        FXMLLoader loader = SceneManager.switchTo("analysis.fxml");
        AnalysisController controller = loader.getController();
        controller.initWithCV(cv, new AnalysisService());
    }

    // ── Save (no analysis) ────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        if (currentSection != null) {
            sectionContents.put(currentSection, editorArea.getText());
        }

        boolean hasContent = sectionContents.values().stream()
            .anyMatch(c -> !c.isBlank());
        if (!hasContent) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                "Please add some content to at least one section before saving.",
                ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        cvService.saveNewCV(buildSections());
        SceneManager.switchTo("dashboard.fxml");
    }

    // ── Back ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleBack() {
        SceneManager.switchTo("dashboard.fxml");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<CVSection> buildSections() {
        List<CVSection> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : sectionContents.entrySet()) {
            String name    = entry.getKey();
            String content = entry.getValue().trim();
            if (content.isEmpty()) continue;  // skip blank sections

            CVSection section = new CVSection();
            section.setTitle(name);
            section.setContent(content);
            section.setType(mapToSectionType(name));
            // cvId and id are assigned by CVService.saveNewCV()
            result.add(section);
        }
        return result;
    }

    private SectionType mapToSectionType(String name) {
        return switch (name.toLowerCase()) {
            case "summary", "objective", "profile" -> SectionType.SUMMARY;
            case "experience", "work experience", "employment" -> SectionType.EXPERIENCE;
            case "education" -> SectionType.EDUCATION;
            case "skills", "technical skills", "competencies" -> SectionType.SKILLS;
            case "projects", "portfolio" -> SectionType.PROJECTS;
            default -> SectionType.OTHER;
        };
    }
}
