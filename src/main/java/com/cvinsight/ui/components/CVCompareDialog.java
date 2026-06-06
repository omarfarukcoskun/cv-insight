package com.cvinsight.ui.components;

import com.cvinsight.ai.client.ClaudeApiClient;
import com.cvinsight.db.dao.ScoreDao;
import com.cvinsight.model.CV;
import com.cvinsight.model.ComparisonResult;
import com.cvinsight.model.Score;
import com.cvinsight.model.SectionComparison;
import com.cvinsight.service.CVService;
import com.cvinsight.ui.SceneManager;
import com.cvinsight.ui.controllers.CompareController;
import com.google.gson.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Two-step modal: (1) pick a resume from the database,
 * (2) show loading while the AI compares both CVs.
 * On success, navigates to compare.fxml.
 */
public class CVCompareDialog {

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void show(Window owner, CV exampleCV) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setWidth(480);
        stage.setHeight(420);
        stage.setResizable(false);
        stage.setTitle("Select a Resume to Compare");

        VBox root = new VBox();
        root.getChildren().setAll(buildPickerContent(stage, root, exampleCV));

        stage.setScene(new Scene(root));
        stage.setOnShown(e -> centerOnScreen(stage));
        stage.show();
    }

    // ── Step 1: Resume picker ─────────────────────────────────────────────────

    private static VBox buildPickerContent(Stage stage, VBox root, CV exampleCV) {

        // Header
        Label header = new Label("Select a resume to compare");
        header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        HBox headerBox = new HBox(header);
        headerBox.setStyle("-fx-padding: 18 20 14 20; -fx-background-color: white; "
            + "-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        // Card list
        VBox cardList = new VBox(8);
        cardList.setStyle("-fx-padding: 12 16 12 16;");

        List<CV>  resumes   = new CVService().getHistory();
        ScoreDao  scoreDao  = new ScoreDao();

        if (resumes.isEmpty()) {
            Label noCV = new Label("No resumes uploaded yet");
            noCV.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
            Button uploadBtn = new Button("Upload a CV");
            uploadBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; "
                + "-fx-padding: 8 18 8 18; -fx-cursor: hand;");
            uploadBtn.setOnAction(e -> { stage.close(); SceneManager.switchTo("upload.fxml"); });
            VBox empty = new VBox(12, noCV, uploadBtn);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-padding: 40;");
            cardList.getChildren().add(empty);
        } else {
            for (CV cv : resumes) {
                int scoreVal = -1;
                try {
                    Optional<Score> s = scoreDao.findByCvId(cv.getId());
                    if (s.isPresent()) scoreVal = s.get().getOverall();
                } catch (SQLException ignored) {}

                HBox card = buildResumeCard(cv, scoreVal);
                card.setOnMouseClicked(e -> {
                    root.getChildren().setAll(buildLoadingContent());
                    startAnalysis(stage, cv, exampleCV);
                });
                cardList.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(cardList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Footer
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: white; -fx-text-fill: #374151; "
            + "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 8; "
            + "-fx-background-radius: 8; -fx-padding: 8 22 8 22; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());
        HBox footer = new HBox(cancelBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-padding: 12 20 14 20; -fx-background-color: white; "
            + "-fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        VBox content = new VBox(headerBox, scroll, footer);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return content;
    }

    private static HBox buildResumeCard(CV cv, int scoreVal) {
        boolean hasFile  = cv.getSourceFile() != null;
        String  filename = hasFile
            ? java.nio.file.Path.of(cv.getSourceFile()).getFileName().toString()
            : (cv.getOwnerName() != null ? cv.getOwnerName() : "My Resume");

        // Icon badge
        StackPane icon = new StackPane();
        icon.setMinSize(36, 36);
        icon.setMaxSize(36, 36);
        icon.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 8;");
        Label iconLbl = new Label(hasFile ? "PDF" : "CV");
        iconLbl.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: #4f46e5;");
        icon.getChildren().add(iconLbl);

        // Info
        Label nameLbl = new Label(filename);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        nameLbl.setMaxWidth(250);

        Label dateLbl = new Label("Uploaded " + cv.getUploadedAt().format(DATE_FMT));
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        VBox info = new VBox(3, nameLbl, dateLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox card = new HBox(12, icon, info);
        card.setAlignment(Pos.CENTER_LEFT);

        // Score badge
        if (scoreVal >= 0) {
            String bg = scoreVal >= 75 ? "#dcfce7" : scoreVal >= 50 ? "#fef9c3" : "#fee2e2";
            String fg = scoreVal >= 75 ? "#166534" : scoreVal >= 50 ? "#854d0e" : "#991b1b";
            Label scoreLbl = new Label(scoreVal + " / 100");
            scoreLbl.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-padding: 3 8 3 8;");
            card.getChildren().add(scoreLbl);
        }

        String normalStyle = "-fx-background-color: white; -fx-background-radius: 8; "
            + "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 8; "
            + "-fx-padding: 12 14 12 14;";
        String hoverStyle  = "-fx-background-color: #f8faff; -fx-background-radius: 8; "
            + "-fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-border-radius: 8; "
            + "-fx-padding: 12 14 12 14;";

        card.setStyle(normalStyle);
        card.setCursor(Cursor.HAND);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e  -> card.setStyle(normalStyle));

        return card;
    }

    // ── Step 2: Loading state ─────────────────────────────────────────────────

    private static VBox buildLoadingContent() {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-accent: #4f46e5;");
        spinner.setMaxSize(44, 44);

        Label lbl = new Label("Analyzing CVs…");
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label sub = new Label("This may take a few seconds.");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        VBox loading = new VBox(14, spinner, lbl, sub);
        loading.setAlignment(Pos.CENTER);
        loading.setStyle("-fx-background-color: #f8fafc;");
        loading.setPrefHeight(420);
        VBox.setVgrow(loading, Priority.ALWAYS);

        VBox root = new VBox(loading);
        VBox.setVgrow(loading, Priority.ALWAYS);
        return root;
    }

    // ── Analysis pipeline ─────────────────────────────────────────────────────

    private static void startAnalysis(Stage stage, CV userCV, CV exampleCV) {
        Thread.ofVirtual().start(() -> {
            try {
                String userText    = extractUserText(userCV);
                String exampleText = extractExampleText(exampleCV);
                String prompt      = buildPrompt(userText, exampleText);
                String response    = new ClaudeApiClient().send(prompt, 2048);
                ComparisonResult result = parseResponse(response);

                Platform.runLater(() -> {
                    stage.close();
                    FXMLLoader loader = SceneManager.switchTo("compare.fxml");
                    CompareController ctl = loader.getController();
                    ctl.init(exampleCV, result);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    stage.close();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        });
    }

    // ── Text extraction ───────────────────────────────────────────────────────

    private static String extractUserText(CV cv) throws Exception {
        if (cv.getRawText() != null && !cv.getRawText().isBlank()) return cv.getRawText();
        if (cv.getSourceFile() != null) {
            File file = new File(cv.getSourceFile());
            if (file.exists()) {
                try (PDDocument doc = Loader.loadPDF(file)) {
                    return new PDFTextStripper().getText(doc);
                } catch (IOException e) {
                    throw new Exception(
                        "Could not read the PDF. Make sure it is a text-based (not scanned) PDF.");
                }
            }
        }
        throw new Exception("No content available for this CV.");
    }

    private static String extractExampleText(CV cv) throws Exception {
        if (cv.getRawText() != null && !cv.getRawText().isBlank()) return cv.getRawText();
        String pdf = cv.getSourceFile();
        if (pdf != null && !pdf.isBlank()) {
            try (InputStream is = CVCompareDialog.class.getResourceAsStream("/examples/" + pdf)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    try (PDDocument doc = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
                        return new PDFTextStripper().getText(doc);
                    } catch (IOException e) {
                        throw new Exception(
                            "Could not read the PDF. Make sure it is a text-based (not scanned) PDF.");
                    }
                }
            } catch (IOException ignored) {}
        }
        throw new Exception("Example CV file not found. Please reinstall the application.");
    }

    // ── Prompt ────────────────────────────────────────────────────────────────

    private static String buildPrompt(String userText, String exampleText) {
        return "You are an expert CV reviewer and career coach. Compare these two CVs.\n\n"
            + "USER CV (the person seeking advice):\n" + userText + "\n\n"
            + "EXAMPLE CV (a high-performing professional):\n" + exampleText + "\n\n"
            + "Respond ONLY with a valid JSON object. No markdown, no code fences, "
            + "no explanation outside the JSON. Use exactly this structure:\n"
            + "{\n"
            + "  \"sections\": [\n"
            + "    {\n"
            + "      \"name\": \"Summary / About Me\",\n"
            + "      \"userContent\": \"one sentence describing what the user has in this section\",\n"
            + "      \"exampleContent\": \"one sentence describing what the example has\",\n"
            + "      \"verdict\": \"worse\",\n"
            + "      \"tip\": \"specific actionable improvement advice\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"overallTips\": [\"tip1\", \"tip2\", \"tip3\"],\n"
            + "  \"userScore\": 70,\n"
            + "  \"exampleScore\": 93\n"
            + "}\n\n"
            + "Sections to analyze (include all 5): "
            + "Summary/About Me, Education, Experience, Projects, Skills & Languages.\n\n"
            + "verdict values (from the USER's perspective):\n"
            + "  'worse'  = user CV is clearly weaker in this section\n"
            + "  'equal'  = similar quality\n"
            + "  'better' = user CV is stronger here\n\n"
            + "tip: only fill this if verdict is 'worse', otherwise empty string.";
    }

    // ── Response parsing with truncation repair ───────────────────────────────

    private static ComparisonResult parseResponse(String raw) throws Exception {
        String json = raw.trim();
        if (json.startsWith("```")) {
            int first = json.indexOf('\n'), last = json.lastIndexOf("```");
            if (first > 0 && last > first) json = json.substring(first + 1, last).trim();
        }
        String[] suffixes = { "", "}", "]}", "\"]}}", "\"]}}" };
        Exception last = null;
        for (String s : suffixes) {
            try { return parseJson(json + s); }
            catch (Exception e) { last = e; }
        }
        throw new Exception("Unexpected response format. Please try again.");
    }

    private static ComparisonResult parseJson(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        int userScore    = root.has("userScore")    ? root.get("userScore").getAsInt()    : 0;
        int exampleScore = root.has("exampleScore") ? root.get("exampleScore").getAsInt() : 0;

        List<String> tips = new ArrayList<>();
        if (root.has("overallTips"))
            for (JsonElement e : root.getAsJsonArray("overallTips")) tips.add(e.getAsString());

        List<SectionComparison> sections = new ArrayList<>();
        if (root.has("sections"))
            for (JsonElement e : root.getAsJsonArray("sections")) {
                JsonObject s = e.getAsJsonObject();
                sections.add(new SectionComparison(
                    s.has("name")           ? s.get("name").getAsString()           : "",
                    s.has("userContent")    ? s.get("userContent").getAsString()    : "",
                    s.has("exampleContent") ? s.get("exampleContent").getAsString() : "",
                    s.has("verdict")        ? s.get("verdict").getAsString()        : "equal",
                    s.has("tip")            ? s.get("tip").getAsString()            : ""
                ));
            }
        return new ComparisonResult(sections, tips, userScore, exampleScore);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void centerOnScreen(Stage stage) {
        javafx.geometry.Rectangle2D b = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX((b.getWidth()  - stage.getWidth())  / 2);
        stage.setY((b.getHeight() - stage.getHeight()) / 2);
    }
}
