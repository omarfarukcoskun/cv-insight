package com.cvinsight.ui.controllers;

import com.cvinsight.model.CV;
import com.cvinsight.service.ComparisonService;
import com.cvinsight.service.CVService;
import com.cvinsight.ui.SceneManager;
import com.cvinsight.ui.components.CVCompareDialog;
import com.cvinsight.ui.components.PDFPreviewDialog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
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
import java.net.URL;
import java.util.*;

public class ComparisonController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label     countLabel;
    @FXML private VBox      examplesContainer;
    @FXML private VBox      noResultsPane;
    @FXML private Label     noResultsTitle;

    private final ComparisonService comparisonService = new ComparisonService();
    private final CVService         cvService         = new CVService();

    private CV  userCV;
    private int cardColorIndex = 0;
    private final Map<String, Label> matchBadges = new HashMap<>();

    private static final String[][] CARD_COLORS = {
        { "#f0fdf4", "#16a34a" },
        { "#eff6ff", "#2563eb" },
        { "#fffbeb", "#d97706" },
        { "#f5f3ff", "#7c3aed" },
        { "#fff1f2", "#e11d48" },
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resolveUserCV();
        List<CV> examples = comparisonService.getAllExamples();
        loadExamples(examples);
        searchField.textProperty().addListener(
            (obs, oldVal, newVal) -> filterExamples(newVal));
        if (userCV != null) computeMatchScores(examples);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void filterExamples(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        long visible = 0;
        for (Node node : examplesContainer.getChildren()) {
            if (node instanceof HBox card) {
                boolean matches = q.isEmpty()
                    || (card.getUserData() instanceof String s && s.contains(q));
                card.setVisible(matches);
                card.setManaged(matches);
                if (matches) visible++;
            }
        }
        countLabel.setText(visible + " EXAMPLE" + (visible != 1 ? "S" : ""));
        boolean empty = (visible == 0);
        noResultsPane.setVisible(empty);
        noResultsPane.setManaged(empty);
        noResultsTitle.setText(!q.isEmpty()
            ? "No results for \"" + query.trim() + "\""
            : "No examples available");
    }

    // ── Example list ─────────────────────────────────────────────────────────

    private void loadExamples(List<CV> examples) {
        examplesContainer.getChildren().clear();
        matchBadges.clear();
        cardColorIndex = 0;
        countLabel.setText(examples.size() + " EXAMPLE" + (examples.size() != 1 ? "S" : ""));

        if (examples.isEmpty()) {
            noResultsTitle.setText("No examples available");
            noResultsPane.setVisible(true);
            noResultsPane.setManaged(true);
            return;
        }
        noResultsPane.setVisible(false);
        noResultsPane.setManaged(false);

        // -2 = loading (will be filled async), -1 = no user CV
        int initialScore = (userCV != null) ? -2 : -1;
        for (CV cv : examples) {
            examplesContainer.getChildren().add(buildExampleCard(cv, initialScore));
        }
    }

    // ── Card builder ─────────────────────────────────────────────────────────

    private HBox buildExampleCard(CV cv, int similarityScore) {
        String[] colors = CARD_COLORS[cardColorIndex % CARD_COLORS.length];
        cardColorIndex++;

        // Parse ownerName: "PersonName | Company — Role"
        String ownerName = cv.getOwnerName() != null ? cv.getOwnerName() : "";
        int pipe = ownerName.indexOf(" | ");
        String personName = pipe >= 0 ? ownerName.substring(0, pipe) : "";
        String titleText  = pipe >= 0 ? ownerName.substring(pipe + 3) : ownerName;

        // ── Icon ──────────────────────────────────────────────────────────────
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(36, 36);
        iconBox.setMaxSize(36, 36);
        iconBox.setStyle("-fx-background-color: " + colors[0] + "; -fx-background-radius: 8;");
        Label iconLabel = new Label("CV");
        iconLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: " + colors[1] + ";");
        iconBox.getChildren().add(iconLabel);

        // ── Title + person name ───────────────────────────────────────────────
        Label titleLbl = new Label(titleText);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #0f172a;");
        titleLbl.setMaxWidth(Double.MAX_VALUE);

        VBox titleBlock = new VBox(2, titleLbl);
        if (!personName.isBlank()) {
            Label personLbl = new Label(personName);
            personLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
            titleBlock.getChildren().add(personLbl);
        }

        // ── Bottom row: match pill + company ─────────────────────────────────
        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        if (similarityScore == -2) {
            Label loadingPill = new Label("...");
            loadingPill.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; "
                + "-fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 2 8 2 8;");
            matchBadges.put(cv.getId(), loadingPill);
            bottomRow.getChildren().add(loadingPill);
        } else if (similarityScore >= 0) {
            bottomRow.getChildren().add(buildMatchPill(similarityScore));
        } else {
            Label dashPill = new Label("—");
            dashPill.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; "
                + "-fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 2 8 2 8;");
            bottomRow.getChildren().add(dashPill);
        }

        int dash = titleText.indexOf(" — ");
        String company = dash >= 0 ? titleText.substring(0, dash) : titleText;
        Label metaLabel = new Label(company);
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        bottomRow.getChildren().add(metaLabel);

        titleBlock.getChildren().add(bottomRow);
        HBox.setHgrow(titleBlock, Priority.ALWAYS);

        // ── Buttons ───────────────────────────────────────────────────────────
        Button viewBtn = new Button("View");
        viewBtn.setStyle(
            "-fx-background-color: #f1f5f9; -fx-text-fill: #374151; "
            + "-fx-font-size: 13px; -fx-background-radius: 8; "
            + "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; "
            + "-fx-padding: 6 14 6 14; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> showViewDialog(cv, titleText, viewBtn));

        Button compareBtn = new Button("Compare");
        compareBtn.setStyle(
            "-fx-background-color: #E6F1FB; -fx-text-fill: #185FA5; "
            + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; "
            + "-fx-border-color: #B5D4F4; -fx-border-width: 1; -fx-border-radius: 8; "
            + "-fx-padding: 6 14 6 14; -fx-cursor: hand;");
        if (userCV == null) {
            compareBtn.setDisable(true);
            compareBtn.setTooltip(new Tooltip("Analyse a CV first to enable comparison"));
        }
        compareBtn.setOnAction(e ->
            CVCompareDialog.show(compareBtn.getScene().getWindow(), cv));

        HBox btns = new HBox(8, viewBtn, compareBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        // ── Card ──────────────────────────────────────────────────────────────
        HBox card = new HBox(14, iconBox, titleBlock, btns);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12; "
            + "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 12; "
            + "-fx-padding: 14 18 14 18;");

        // Searchable text: titleText + personName + category (userId)
        String searchText = ownerName.toLowerCase() + " "
            + (cv.getUserId() != null ? cv.getUserId().toLowerCase() : "");
        card.setUserData(searchText);

        return card;
    }

    // ── Match score computation ───────────────────────────────────────────────

    private void computeMatchScores(List<CV> examples) {
        Thread.ofVirtual().start(() -> {
            try {
                String userText    = extractCVText(userCV);
                Set<String> userKw = extractKeywords(userText);

                for (CV example : examples) {
                    try {
                        String exText;
                        String pdf = example.getSourceFile();
                        if (pdf != null && !pdf.isBlank()) {
                            exText = extractExampleText(pdf);
                        } else if (example.getRawText() != null) {
                            exText = example.getRawText();
                        } else {
                            continue;
                        }
                        Set<String> exKw = extractKeywords(exText);
                        int score = calculateMatchScore(userKw, exKw);
                        String id = example.getId();
                        Platform.runLater(() -> updateMatchBadge(id, score));
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        });
    }

    private void updateMatchBadge(String cvId, int score) {
        Label pill = matchBadges.get(cvId);
        if (pill == null) return;
        String bg, fg;
        if (score <= 40)      { bg = "#FCEBEB"; fg = "#E24B4A"; }
        else if (score <= 70) { bg = "#FAEEDA"; fg = "#854F0B"; }
        else                  { bg = "#E1F5EE"; fg = "#085041"; }
        pill.setText("Match " + score + "%");
        pill.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; "
            + "-fx-font-size: 11px; -fx-font-weight: bold; "
            + "-fx-background-radius: 6; -fx-padding: 2 8 2 8;");
    }

    private static Label buildMatchPill(int score) {
        String bg, fg;
        if (score <= 40)      { bg = "#FCEBEB"; fg = "#E24B4A"; }
        else if (score <= 70) { bg = "#FAEEDA"; fg = "#854F0B"; }
        else                  { bg = "#E1F5EE"; fg = "#085041"; }
        Label pill = new Label("Match " + score + "%");
        pill.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; "
            + "-fx-font-size: 11px; -fx-font-weight: bold; "
            + "-fx-background-radius: 6; -fx-padding: 2 8 2 8;");
        return pill;
    }

    private static String extractCVText(CV cv) throws Exception {
        if (cv.getRawText() != null && !cv.getRawText().isBlank()) return cv.getRawText();
        if (cv.getSourceFile() != null) {
            File file = new File(cv.getSourceFile());
            if (file.exists()) {
                try (PDDocument doc = Loader.loadPDF(file)) {
                    return new PDFTextStripper().getText(doc);
                }
            }
        }
        throw new Exception("No content for user CV");
    }

    private static String extractExampleText(String pdfFilename) throws Exception {
        try (InputStream stream = ComparisonController.class.getResourceAsStream("/examples/" + pdfFilename)) {
            if (stream == null) throw new Exception("Not found: " + pdfFilename);
            byte[] bytes = stream.readAllBytes();
            try (PDDocument doc = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
                return new PDFTextStripper().getText(doc);
            }
        }
    }

    private static Set<String> extractKeywords(String text) {
        Set<String> stopWords = Set.of(
            "the", "and", "for", "with", "that", "this", "have", "from",
            "are", "was", "were", "been", "they", "their", "will", "your",
            "has", "had", "not", "but", "can", "also", "into", "more",
            "over", "than", "then", "when", "what", "some", "such", "both",
            "each", "its", "our", "all", "any", "other", "which", "about",
            "would", "could", "should", "these", "those", "using", "used"
        );
        Set<String> keywords = new HashSet<>();
        for (String token : text.toLowerCase().split("[\\s\\p{Punct}]+")) {
            if (token.length() > 3 && !stopWords.contains(token)) {
                keywords.add(token);
            }
        }
        return keywords;
    }

    private static int calculateMatchScore(Set<String> userKeywords, Set<String> exKeywords) {
        if (exKeywords.isEmpty()) return 0;
        long overlap = exKeywords.stream().filter(userKeywords::contains).count();
        int score = (int) Math.round((double) overlap / exKeywords.size() * 100);
        return Math.min(score, 99);
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private void showViewDialog(CV cv, String title, Button trigger) {
        String pdfFilename = cv.getSourceFile(); // holds pdf_filename for example CVs
        Window owner = trigger.getScene().getWindow();

        if (pdfFilename != null && !pdfFilename.isBlank()) {
            try (InputStream is = getClass().getResourceAsStream("/examples/" + pdfFilename)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    PDFPreviewDialog.showFromBytes(owner, bytes, title);
                    return;
                }
            } catch (IOException ignored) {}
        }
        // Fallback: show raw CV text in a scrollable dialog
        showRawTextDialog(owner, title, cv.getRawText());
    }

    private void showRawTextDialog(Window owner, String title, String rawText) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);
        dialog.setWidth(860);
        dialog.setHeight(700);

        WebView webView = new WebView();
        webView.getEngine().loadContent(buildCVHtml(title, rawText), "text/html");

        VBox layout = new VBox(webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
        dialog.setScene(new Scene(layout));
        dialog.show();
    }

    private static String buildCVHtml(String title, String rawText) {
        if (rawText == null || rawText.isBlank()) rawText = "(no content)";

        // Known section headings to style with blue underlined headers
        java.util.Set<String> HEADERS = java.util.Set.of(
            "ABOUT ME", "EDUCATION", "EXPERIENCE", "SKILLS", "PROJECTS",
            "SKILLS & LANGUAGES", "SKILLS & AREAS", "PUBLICATIONS",
            "SELECTED PUBLICATIONS", "PUBLICATIONS & TALKS", "ACADEMIC EXPERIENCE"
        );

        String[] lines = rawText.split("\n");
        StringBuilder body = new StringBuilder();
        boolean nameRendered = false;

        for (int i = 0; i < lines.length; i++) {
            String raw  = lines[i];
            String line = raw.trim();

            if (line.isBlank()) {
                body.append("<div style='height:8px'></div>");
                continue;
            }

            // First non-blank line → name header
            if (!nameRendered) {
                body.append("<div class='name'>").append(esc(line)).append("</div>");
                nameRendered = true;
                continue;
            }

            // Second line (contact info, often contains •) → subtitle
            if (i == 1 || (i <= 3 && line.contains("•") && !line.startsWith("•"))) {
                body.append("<div class='contact'>").append(esc(line)).append("</div>");
                continue;
            }

            // Section header
            if (HEADERS.contains(line.toUpperCase())) {
                body.append("<div class='sec-title'>").append(esc(line)).append("</div>")
                    .append("<hr class='sec-rule'/>");
                continue;
            }

            // Bullet point
            if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*")) {
                String bullet = line.replaceFirst("^[•\\-\\*]\\s*", "");
                body.append("<div class='bullet'>• ").append(esc(bullet)).append("</div>");
                continue;
            }

            // Bold-looking entry headers (e.g. "Senior Staff Software Engineer — Google")
            if (line.length() < 90 && (line.contains("—") || line.contains("|")) && !line.startsWith(" ")) {
                body.append("<div class='entry-title'>").append(esc(line)).append("</div>");
                continue;
            }

            body.append("<div class='line'>").append(esc(line)).append("</div>");
        }

        return """
            <!DOCTYPE html><html><head><meta charset='UTF-8'/><style>
            * { box-sizing:border-box; margin:0; padding:0; }
            body { background:#c8c8c8; font-family:'Georgia',serif; font-size:11pt; color:#111; }
            .page { width:794px; min-height:1123px; background:white;
                    margin:24px auto; padding:48px 52px;
                    box-shadow:0 4px 24px rgba(0,0,0,.3); }
            .name { font-size:22pt; font-weight:bold; margin-bottom:5px; }
            .contact { font-size:9pt; color:#555; margin-bottom:10px; }
            .sec-title { font-size:10pt; font-weight:bold; color:#1a56db;
                         text-transform:uppercase; letter-spacing:.4px; margin-top:14px; }
            .sec-rule { border:none; border-top:.75px solid #aaa; margin:2px 0 8px; }
            .entry-title { font-weight:bold; font-size:11pt; margin-top:8px; }
            .bullet { font-size:10.5pt; margin-left:14px; margin-bottom:3px; line-height:1.5; }
            .line { font-size:10.5pt; margin-bottom:2px; line-height:1.55; }
            </style></head><body><div class='page'>
            """ + body + """
            </div></body></html>
            """;
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void resolveUserCV() {
        List<CV> history = cvService.getHistory();
        if (!history.isEmpty()) userCV = history.get(0);
    }

    @FXML private void handleBack() { SceneManager.switchTo("dashboard.fxml"); }
}
