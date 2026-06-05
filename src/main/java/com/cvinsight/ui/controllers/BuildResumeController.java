package com.cvinsight.ui.controllers;

import com.cvinsight.cv.parser.CVParseException;
import com.cvinsight.model.CV;
import com.cvinsight.model.CVSection;
import com.cvinsight.model.SectionType;
import com.cvinsight.service.CVService;
import com.cvinsight.ui.SceneManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class BuildResumeController implements Initializable {

    @FXML private VBox    sectionsContainer;
    @FXML private WebView previewWebView;

    // ── Personal info simple fields ───────────────────────────────────────────
    private TextField nameField, locationField, phoneField, emailField, linkedinField, websiteField;
    private TextArea  aboutField;
    private String    photoBase64 = "";

    // ── Repeatable entry data ─────────────────────────────────────────────────
    private final List<EduEntry>  eduEntries  = new ArrayList<>();
    private final List<ExpEntry>  expEntries  = new ArrayList<>();
    private final List<ProjEntry> projEntries = new ArrayList<>();
    private final List<LangEntry> langEntries = new ArrayList<>();
    private final List<String>    techSkills  = new ArrayList<>();
    private final List<String>    softSkills  = new ArrayList<>();

    // ── Entry list containers ─────────────────────────────────────────────────
    private VBox      eduContainer, expContainer, projContainer, langContainer;
    private FlowPane  techSkillsPane, softSkillsPane;

    private final CVService cvService = new CVService();

    // ── Inner data classes ────────────────────────────────────────────────────

    static class EduEntry {
        final StringProperty institution = new SimpleStringProperty("");
        final StringProperty degree      = new SimpleStringProperty("");
        final StringProperty start       = new SimpleStringProperty("");
        final StringProperty end         = new SimpleStringProperty("");
    }

    static class ExpEntry {
        final StringProperty title       = new SimpleStringProperty("");
        final StringProperty company     = new SimpleStringProperty("");
        final StringProperty location    = new SimpleStringProperty("");
        final StringProperty start       = new SimpleStringProperty("");
        final StringProperty end         = new SimpleStringProperty("");
        final StringProperty description = new SimpleStringProperty("");
    }

    static class ProjEntry {
        final StringProperty name        = new SimpleStringProperty("");
        final StringProperty subtitle    = new SimpleStringProperty("");
        final StringProperty description = new SimpleStringProperty("");
    }

    static class LangEntry {
        final StringProperty language = new SimpleStringProperty("");
        final StringProperty level    = new SimpleStringProperty("Intermediate");
    }

    // ── Initialization ────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildLeftPanel();
        addEduEntry(new EduEntry());
        addExpEntry(new ExpEntry());
        updatePreview();
    }

    private void buildLeftPanel() {
        sectionsContainer.getChildren().addAll(
            buildSection("👤  Personal Info",   buildPersonalInfoContent()),
            buildSection("📝  About Me",         buildAboutContent()),
            buildSection("🎓  Education",         buildEducationContent()),
            buildSection("💼  Experience",        buildExperienceContent()),
            buildSection("🚀  Projects",          buildProjectsContent()),
            buildSection("🛠  Technical Skills", buildTechSkillsContent()),
            buildSection("💡  Soft Skills",       buildSoftSkillsContent()),
            buildSection("🌍  Languages",         buildLanguagesContent())
        );
    }

    // ── Generic collapsible section ───────────────────────────────────────────

    private VBox buildSection(String title, VBox content) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label arrow = new Label("▼");
        arrow.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        HBox header = new HBox(8, titleLabel, arrow);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(11, 16, 11, 16));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        content.setPadding(new Insets(12, 16, 14, 16));
        content.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        header.setOnMouseClicked(e -> {
            boolean wasVisible = content.isVisible();
            content.setVisible(!wasVisible);
            content.setManaged(!wasVisible);
            arrow.setText(wasVisible ? "▶" : "▼");
        });

        return new VBox(header, content);
    }

    // ── Section content builders ──────────────────────────────────────────────

    private VBox buildPersonalInfoContent() {
        nameField     = trackedField("Full Name");
        locationField = trackedField("Location");
        phoneField    = trackedField("Phone");
        emailField    = trackedField("Email");
        linkedinField = trackedField("LinkedIn URL");
        websiteField  = trackedField("Website");

        Button photoBtn = new Button("📷  Upload Photo");
        photoBtn.getStyleClass().add("secondary-button");
        photoBtn.setMaxWidth(Double.MAX_VALUE);
        photoBtn.setOnAction(e -> handlePhotoUpload());

        VBox box = new VBox(8, nameField, locationField, phoneField, emailField,
                            linkedinField, websiteField, photoBtn);
        return box;
    }

    private VBox buildAboutContent() {
        aboutField = new TextArea();
        aboutField.setPromptText("Write a short professional summary...");
        aboutField.setPrefRowCount(4);
        aboutField.setWrapText(true);
        aboutField.textProperty().addListener((obs, o, v) -> updatePreview());
        return new VBox(aboutField);
    }

    private VBox buildEducationContent() {
        eduContainer = new VBox(8);
        Button addBtn = new Button("＋  Add Education");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addEduEntry(new EduEntry()));
        return new VBox(8, eduContainer, addBtn);
    }

    private VBox buildExperienceContent() {
        expContainer = new VBox(8);
        Button addBtn = new Button("＋  Add Experience");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addExpEntry(new ExpEntry()));
        return new VBox(8, expContainer, addBtn);
    }

    private VBox buildProjectsContent() {
        projContainer = new VBox(8);
        Button addBtn = new Button("＋  Add Project");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addProjEntry(new ProjEntry()));
        return new VBox(8, projContainer, addBtn);
    }

    private VBox buildTechSkillsContent() {
        techSkillsPane = new FlowPane(6, 6);
        TextField input = new TextField();
        input.setPromptText("Type a skill and press Enter");
        input.setOnAction(e -> {
            String s = input.getText().trim();
            if (!s.isEmpty() && !techSkills.contains(s)) {
                techSkills.add(s);
                techSkillsPane.getChildren().add(skillTag(s, techSkills, techSkillsPane));
                updatePreview();
            }
            input.clear();
        });
        return new VBox(8, input, techSkillsPane);
    }

    private VBox buildSoftSkillsContent() {
        softSkillsPane = new FlowPane(6, 6);
        TextField input = new TextField();
        input.setPromptText("Type a skill and press Enter");
        input.setOnAction(e -> {
            String s = input.getText().trim();
            if (!s.isEmpty() && !softSkills.contains(s)) {
                softSkills.add(s);
                softSkillsPane.getChildren().add(skillTag(s, softSkills, softSkillsPane));
                updatePreview();
            }
            input.clear();
        });
        return new VBox(8, input, softSkillsPane);
    }

    private VBox buildLanguagesContent() {
        langContainer = new VBox(8);
        Button addBtn = new Button("＋  Add Language");
        addBtn.getStyleClass().add("secondary-button");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addLangEntry(new LangEntry()));
        return new VBox(8, langContainer, addBtn);
    }

    // ── Repeatable entry adders ───────────────────────────────────────────────

    private void addEduEntry(EduEntry entry) {
        eduEntries.add(entry);
        watchProps(entry.institution, entry.degree, entry.start, entry.end);

        TextField instF  = boundField("Institution", entry.institution);
        TextField degF   = boundField("Degree / Field of Study", entry.degree);
        TextField startF = boundField("Start Year", entry.start);
        TextField endF   = boundField("End Year",   entry.end);
        startF.setPrefWidth(90); endF.setPrefWidth(90);

        HBox dates = new HBox(6, new Label("From"), startF, new Label("To"), endF);
        dates.setAlignment(Pos.CENTER_LEFT);

        VBox row = new VBox(6);
        row.setStyle(entryStyle());
        Button del = deleteBtn(() -> { eduEntries.remove(entry); eduContainer.getChildren().remove(row); updatePreview(); });
        row.getChildren().addAll(rowHeader("Education Entry", del), instF, degF, dates);

        eduContainer.getChildren().add(row);
        updatePreview();
    }

    private void addExpEntry(ExpEntry entry) {
        expEntries.add(entry);
        watchProps(entry.title, entry.company, entry.location, entry.start, entry.end, entry.description);

        TextField titleF = boundField("Job Title",  entry.title);
        TextField compF  = boundField("Company",    entry.company);
        TextField locF   = boundField("Location",   entry.location);
        TextField startF = boundField("Start Date", entry.start);
        TextField endF   = boundField("End Date",   entry.end);
        TextArea  descF  = boundArea("Responsibilities (one per line)", entry.description);
        HBox.setHgrow(compF, Priority.ALWAYS); HBox.setHgrow(locF, Priority.ALWAYS);
        HBox compRow = new HBox(8, compF, locF);
        HBox dateRow = new HBox(8, new Label("From"), startF, new Label("To"), endF);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        VBox row = new VBox(6);
        row.setStyle(entryStyle());
        Button del = deleteBtn(() -> { expEntries.remove(entry); expContainer.getChildren().remove(row); updatePreview(); });
        row.getChildren().addAll(rowHeader("Experience Entry", del), titleF, compRow, dateRow, descF);

        expContainer.getChildren().add(row);
        updatePreview();
    }

    private void addProjEntry(ProjEntry entry) {
        projEntries.add(entry);
        watchProps(entry.name, entry.subtitle, entry.description);

        TextField nameF = boundField("Project Name",   entry.name);
        TextField subF  = boundField("Role / Subtitle", entry.subtitle);
        TextArea  descF = boundArea("Description (one per line)", entry.description);

        VBox row = new VBox(6);
        row.setStyle(entryStyle());
        Button del = deleteBtn(() -> { projEntries.remove(entry); projContainer.getChildren().remove(row); updatePreview(); });
        row.getChildren().addAll(rowHeader("Project Entry", del), nameF, subF, descF);

        projContainer.getChildren().add(row);
        updatePreview();
    }

    private void addLangEntry(LangEntry entry) {
        langEntries.add(entry);
        entry.language.addListener((obs, o, v) -> updatePreview());
        entry.level.addListener((obs, o, v)    -> updatePreview());

        TextField langF = boundField("Language", entry.language);
        HBox.setHgrow(langF, Priority.ALWAYS);

        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Native", "Professional", "Advanced", "Intermediate", "Beginner");
        levelCombo.valueProperty().bindBidirectional(entry.level);
        levelCombo.setValue("Intermediate");
        levelCombo.setOnAction(e -> updatePreview());

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(entry);
        Button del = deleteBtn(() -> { langEntries.remove(entry); langContainer.getChildren().remove(row); updatePreview(); });
        row.getChildren().addAll(langF, levelCombo, del);

        langContainer.getChildren().add(row);
        updatePreview();
    }

    // ── Photo upload ──────────────────────────────────────────────────────────

    private void handlePhotoUpload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Photo");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(SceneManager.getStage());
        if (file != null) {
            try {
                photoBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                updatePreview();
            } catch (Exception ignored) {}
        }
    }

    // ── PDF upload & parse ────────────────────────────────────────────────────

    @FXML
    private void handleUploadPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload Existing CV");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF / Text", "*.pdf", "*.txt"));
        File file = chooser.showOpenDialog(SceneManager.getStage());
        if (file == null) return;

        try {
            CV cv = cvService.loadFromFile(file);
            populateFromCV(cv);
        } catch (CVParseException e) {
            new Alert(Alert.AlertType.ERROR, "Could not read file: " + e.getMessage()).showAndWait();
        }
    }

    private void populateFromCV(CV cv) {
        eduEntries.clear();  eduContainer.getChildren().clear();
        expEntries.clear();  expContainer.getChildren().clear();
        projEntries.clear(); projContainer.getChildren().clear();
        langEntries.clear(); langContainer.getChildren().clear();
        techSkills.clear();  techSkillsPane.getChildren().clear();
        softSkills.clear();  softSkillsPane.getChildren().clear();

        if (cv.getOwnerName() != null) nameField.setText(cv.getOwnerName());

        for (CVSection section : cv.getSections()) {
            String content = section.getContent() != null ? section.getContent().trim() : "";
            String[] lines = content.split("\\n");

            switch (section.getType()) {
                case SUMMARY -> aboutField.setText(content);

                case EXPERIENCE -> {
                    ExpEntry e = new ExpEntry();
                    if (lines.length > 0) e.title.set(lines[0].trim());
                    if (lines.length > 1) e.company.set(lines[1].trim());
                    if (lines.length > 2) e.description.set(
                        String.join("\n", Arrays.copyOfRange(lines, 2, lines.length)));
                    addExpEntry(e);
                }
                case EDUCATION -> {
                    EduEntry e = new EduEntry();
                    if (lines.length > 0) e.institution.set(lines[0].trim());
                    if (lines.length > 1) e.degree.set(lines[1].trim());
                    addEduEntry(e);
                }
                case SKILLS -> {
                    for (String line : lines) {
                        String s = line.replaceAll("^[-•*]\\s*", "").trim();
                        if (!s.isEmpty() && !techSkills.contains(s)) {
                            techSkills.add(s);
                            techSkillsPane.getChildren().add(skillTag(s, techSkills, techSkillsPane));
                        }
                    }
                }
                case PROJECTS -> {
                    ProjEntry p = new ProjEntry();
                    if (lines.length > 0) p.name.set(lines[0].trim());
                    if (lines.length > 1) p.description.set(
                        String.join("\n", Arrays.copyOfRange(lines, 1, lines.length)));
                    addProjEntry(p);
                }
                default -> {}
            }
        }
        updatePreview();
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        List<CVSection> sections = new ArrayList<>();
        addIfNotBlank(sections, SectionType.SUMMARY,    "About Me", aboutField.getText());
        for (EduEntry e : eduEntries)
            addIfNotBlank(sections, SectionType.EDUCATION, e.institution.get(),
                e.institution.get() + "\n" + e.degree.get() + "\n" + e.start.get() + " – " + e.end.get());
        for (ExpEntry e : expEntries)
            addIfNotBlank(sections, SectionType.EXPERIENCE, e.title.get(),
                e.title.get() + "\n" + e.company.get() + "\n" + e.description.get());
        for (ProjEntry p : projEntries)
            addIfNotBlank(sections, SectionType.PROJECTS, p.name.get(),
                p.name.get() + "\n" + p.description.get());
        if (!techSkills.isEmpty())
            addIfNotBlank(sections, SectionType.SKILLS, "Technical Skills", String.join("\n", techSkills));

        if (!sections.isEmpty()) {
            cvService.saveNewCV(sections);
            new Alert(Alert.AlertType.INFORMATION, "Resume saved to your history.", ButtonType.OK).showAndWait();
        } else {
            new Alert(Alert.AlertType.WARNING, "Nothing to save — please fill in at least one section.", ButtonType.OK).showAndWait();
        }
    }

    private void addIfNotBlank(List<CVSection> list, SectionType type, String title, String content) {
        if (content != null && !content.isBlank()) {
            CVSection s = new CVSection();
            s.setType(type); s.setTitle(title); s.setContent(content);
            list.add(s);
        }
    }

    // ── Download PDF ──────────────────────────────────────────────────────────

    @FXML
    private void handleDownloadPdf() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(SceneManager.getStage())) {
            previewWebView.getEngine().print(job);
            job.endJob();
        }
    }

    // ── Back ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleBack() { SceneManager.switchTo("dashboard.fxml"); }

    // ── Live preview ──────────────────────────────────────────────────────────

    private void updatePreview() {
        String html = generateHtml();
        Platform.runLater(() -> previewWebView.getEngine().loadContent(html, "text/html"));
    }

    private String generateHtml() {
        String name     = esc(nameField.getText());
        String location = esc(locationField.getText());
        String phone    = esc(phoneField.getText());
        String email    = esc(emailField.getText());
        String linkedin = esc(linkedinField.getText());
        String website  = esc(websiteField.getText());

        // Contact line
        List<String> cp = new ArrayList<>();
        if (!location.isEmpty()) cp.add(location);
        if (!phone.isEmpty())    cp.add(phone);
        if (!email.isEmpty())    cp.add("<a href='mailto:" + email + "'>" + email + "</a>");
        if (!linkedin.isEmpty()) cp.add("<a href='" + linkedin + "'>LinkedIn</a>");
        if (!website.isEmpty())  cp.add("<a href='" + website + "'>" + website + "</a>");
        String contact = String.join(" &nbsp;|&nbsp; ", cp);

        String photoHtml = photoBase64.isEmpty() ? "<div class='photo-placeholder'></div>"
            : "<img src='data:image/jpeg;base64," + photoBase64 + "' class='photo'/>";

        // Education HTML
        StringBuilder eduHtml = new StringBuilder();
        for (EduEntry e : eduEntries) {
            if (e.institution.get().isBlank() && e.degree.get().isBlank()) continue;
            String dates = (!e.start.get().isBlank() || !e.end.get().isBlank())
                ? esc(e.start.get()) + " – " + esc(e.end.get()) : "";
            eduHtml.append("<div class='entry'>")
                   .append("<div class='row-header'><span class='bold'>").append(esc(e.institution.get())).append("</span>")
                   .append(dates.isEmpty() ? "" : "<span class='dates'>" + dates + "</span>")
                   .append("</div>")
                   .append("<div class='italic small'>").append(esc(e.degree.get())).append("</div>")
                   .append("</div>");
        }

        // Experience HTML
        StringBuilder expHtml = new StringBuilder();
        for (ExpEntry e : expEntries) {
            if (e.title.get().isBlank()) continue;
            String comp  = esc(e.company.get());
            String loc   = esc(e.location.get());
            String dates = esc(e.start.get()) + " – " + esc(e.end.get());
            String right = loc.isEmpty() ? dates : loc + " | " + dates;
            expHtml.append("<div class='entry'>")
                   .append("<div class='row-header'><span class='bold'>").append(esc(e.title.get()))
                   .append(comp.isEmpty() ? "" : " — " + comp)
                   .append("</span><span class='dates'>").append(right).append("</span></div>");
            if (!e.description.get().isBlank()) {
                expHtml.append("<ul>");
                for (String line : e.description.get().split("\\n")) {
                    String l = line.replaceAll("^[-•*]\\s*", "").trim();
                    if (!l.isEmpty()) expHtml.append("<li>").append(esc(l)).append("</li>");
                }
                expHtml.append("</ul>");
            }
            expHtml.append("</div>");
        }

        // Projects HTML
        StringBuilder projHtml = new StringBuilder();
        for (ProjEntry p : projEntries) {
            if (p.name.get().isBlank()) continue;
            projHtml.append("<div class='entry'><div class='bold'>").append(esc(p.name.get()));
            if (!p.subtitle.get().isBlank())
                projHtml.append(" <span class='italic normal'>— ").append(esc(p.subtitle.get())).append("</span>");
            projHtml.append("</div>");
            if (!p.description.get().isBlank()) {
                projHtml.append("<ul>");
                for (String line : p.description.get().split("\\n")) {
                    String l = line.replaceAll("^[-•*]\\s*", "").trim();
                    if (!l.isEmpty()) projHtml.append("<li>").append(esc(l)).append("</li>");
                }
                projHtml.append("</ul>");
            }
            projHtml.append("</div>");
        }

        // Skills HTML
        StringBuilder techHtml = new StringBuilder();
        for (String s : techSkills) techHtml.append("<li>").append(esc(s)).append("</li>");
        StringBuilder softHtml = new StringBuilder();
        for (String s : softSkills) softHtml.append("<li>").append(esc(s)).append("</li>");
        StringBuilder langHtml = new StringBuilder();
        for (LangEntry l : langEntries) {
            if (l.language.get().isBlank()) continue;
            langHtml.append("<div class='lang-row'><span class='bold small'>")
                    .append(esc(l.language.get()))
                    .append("</span><span class='small muted'>")
                    .append(esc(l.level.get())).append("</span></div>");
        }

        boolean hasSkills = !techSkills.isEmpty() || !softSkills.isEmpty() || !langEntries.isEmpty();

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>"
            + buildCss()
            + "</style></head><body><div class='page'>"
            // Header
            + "<div class='header'><div class='header-left'>"
            + "<div class='name'>" + (name.isEmpty() ? "Your Name" : name) + "</div>"
            + "<div class='contact'>" + contact + "</div>"
            + "</div>" + photoHtml + "</div>"
            + "<hr class='divider'/>"
            // About
            + (aboutField.getText().isBlank() ? "" :
                "<div class='sec-title'>ABOUT ME</div><hr class='sec-rule'/>"
                + "<div class='about'>" + esc(aboutField.getText()) + "</div>")
            // Education
            + (eduHtml.isEmpty() ? "" :
                "<div class='sec-title'>EDUCATION</div><hr class='sec-rule'/>" + eduHtml)
            // Experience
            + (expHtml.isEmpty() ? "" :
                "<div class='sec-title'>EXPERIENCES</div><hr class='sec-rule'/>" + expHtml)
            // Projects
            + (projHtml.isEmpty() ? "" :
                "<div class='sec-title'>PROJECTS</div><hr class='sec-rule'/>" + projHtml)
            // Skills & Languages
            + (!hasSkills ? "" :
                "<div class='sec-title'>SKILLS &amp; LANGUAGES</div><hr class='sec-rule'/>"
                + "<div class='skills-grid'>"
                + "<div><div class='col-title'>Technical Skills</div><ul>" + techHtml + "</ul></div>"
                + "<div><div class='col-title'>Soft Skills</div><ul>" + softHtml + "</ul></div>"
                + "<div><div class='col-title'>Languages</div>" + langHtml + "</div>"
                + "</div>")
            + "</div></body></html>";
    }

    private String buildCss() {
        return """
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { background: #c8c8c8; font-family: 'Georgia', serif; font-size: 11pt; color: #111; }
            .page { width: 794px; min-height: 1123px; background: white;
                    margin: 24px auto; padding: 48px 52px;
                    box-shadow: 0 4px 24px rgba(0,0,0,0.3); }
            .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px; }
            .header-left { flex: 1; }
            .name { font-size: 22pt; font-weight: bold; line-height: 1.1; }
            .contact { font-size: 9pt; color: #444; margin-top: 5px; }
            .contact a { color: #1a56db; text-decoration: none; }
            .photo { width: 78px; height: 78px; border-radius: 50%; object-fit: cover; margin-left: 16px; }
            .photo-placeholder { width: 78px; height: 78px; margin-left: 16px; }
            .divider { border: none; border-top: 1.5px solid #222; margin: 8px 0; }
            .sec-title { font-size: 11pt; font-weight: bold; text-decoration: underline;
                         margin: 14px 0 3px; letter-spacing: 0.5px; }
            .sec-rule { border: none; border-top: 0.75px solid #999; margin: 2px 0 8px; }
            .entry { margin-bottom: 10px; }
            .row-header { display: flex; justify-content: space-between; align-items: baseline; }
            .bold { font-weight: bold; }
            .italic { font-style: italic; }
            .normal { font-weight: normal; }
            .small { font-size: 9.5pt; }
            .dates { font-size: 9pt; color: #555; white-space: nowrap; margin-left: 8px; }
            .muted { color: #555; margin-left: 8px; }
            ul { margin: 4px 0 4px 18px; }
            li { font-size: 10pt; margin-bottom: 2px; }
            .about { font-size: 10.5pt; line-height: 1.55; white-space: pre-wrap; }
            .skills-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0 20px; margin-top: 4px; }
            .col-title { font-weight: bold; font-size: 10pt; margin-bottom: 4px; }
            .lang-row { display: flex; justify-content: space-between; margin-bottom: 3px; }
            """;
    }

    // ── Small UI helpers ──────────────────────────────────────────────────────

    private TextField trackedField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.textProperty().addListener((obs, o, v) -> updatePreview());
        return tf;
    }

    private TextField boundField(String prompt, StringProperty prop) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.textProperty().bindBidirectional(prop);
        return tf;
    }

    private TextArea boundArea(String prompt, StringProperty prop) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(3);
        ta.setWrapText(true);
        ta.textProperty().bindBidirectional(prop);
        return ta;
    }

    private void watchProps(StringProperty... props) {
        for (StringProperty p : props) p.addListener((obs, o, v) -> updatePreview());
    }

    private Button deleteBtn(Runnable action) {
        Button b = new Button("✕");
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 0 4 0 4;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private HBox rowHeader(String label, Button del) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af; -fx-font-style: italic;");
        HBox.setHgrow(lbl, Priority.ALWAYS);
        HBox box = new HBox(lbl, del);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Label skillTag(String skill, List<String> list, FlowPane pane) {
        Label tag = new Label(skill + "  ×");
        tag.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #4f46e5; "
            + "-fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-size: 12px;");
        tag.setOnMouseClicked(e -> {
            list.remove(skill);
            pane.getChildren().remove(tag);
            updatePreview();
        });
        return tag;
    }

    private static String entryStyle() {
        return "-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1; "
             + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10;";
    }

    private static String esc(String s) {
        if (s == null || s.isBlank()) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
