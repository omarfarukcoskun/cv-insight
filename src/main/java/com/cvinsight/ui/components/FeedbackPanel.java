package com.cvinsight.ui.components;

import com.cvinsight.model.Feedback;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Displays AI-analysis feedback in three colour-coded sections:
 * Strengths, Weaknesses, Suggestions.
 *
 * Each section has a small coloured dot + heading, a 1px divider between
 * sections, and icon-labelled item rows with softer background tints.
 */
public class FeedbackPanel extends VBox {

    /**
     * Per-section config:
     *   [0] section title
     *   [1] header dot colour
     *   [2] icon glyph (unicode)
     *   [3] icon box background colour
     *   [4] icon glyph colour
     *   [5] item row background colour
     *   [6] item row text colour
     */
    private static final String[][] SECTIONS = {
        { "Strengths",   "#16a34a", "✓", "#bbf7d0", "#15803d", "#E1F5EE", "#166534" },
        { "Weaknesses",  "#ef4444", "!", "#fecaca", "#dc2626", "#FCEBEB", "#991b1b" },
        { "Suggestions", "#3b82f6", "→", "#bfdbfe", "#2563eb", "#E6F1FB", "#1e40af" },
    };

    public FeedbackPanel() {
        setSpacing(0);
        setMaxWidth(Double.MAX_VALUE);
    }

    public void populate(Feedback feedback) {
        getChildren().clear();

        List<List<String>> allItems = List.of(
            feedback.getStrengths(),
            feedback.getWeaknesses(),
            feedback.getSuggestions()
        );

        for (int i = 0; i < SECTIONS.length; i++) {
            if (i > 0) {
                getChildren().add(buildDivider());
            }
            getChildren().add(buildSection(SECTIONS[i], allItems.get(i)));
        }
    }

    // ── Section ───────────────────────────────────────────────────────────────

    private VBox buildSection(String[] cfg, List<String> items) {
        VBox section = new VBox(10);
        section.setMaxWidth(Double.MAX_VALUE);

        // Header: small coloured dot + section title
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + cfg[1] + "; -fx-font-size: 8px;");

        Label title = new Label(cfg[0]);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        HBox header = new HBox(7, dot, title);
        header.setAlignment(Pos.CENTER_LEFT);
        section.getChildren().add(header);

        if (items.isEmpty()) {
            Label empty = new Label("None");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
            section.getChildren().add(empty);
        } else {
            VBox itemsBox = new VBox(7);
            itemsBox.setMaxWidth(Double.MAX_VALUE);
            for (String text : items) {
                itemsBox.getChildren().add(buildItem(text, cfg));
            }
            section.getChildren().add(itemsBox);
        }

        return section;
    }

    // ── Item row ─────────────────────────────────────────────────────────────

    private HBox buildItem(String text, String[] cfg) {
        // Small icon badge (20×20, rounded square)
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(20, 20);
        iconBox.setMaxSize(20, 20);
        iconBox.setStyle(
            "-fx-background-color: " + cfg[3] + ";"
            + "-fx-background-radius: 5;");

        Label iconLabel = new Label(cfg[2]);
        iconLabel.setStyle(
            "-fx-text-fill: " + cfg[4] + ";"
            + "-fx-font-size: 10px;"
            + "-fx-font-weight: bold;");
        iconBox.getChildren().add(iconLabel);

        // Item text
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + cfg[6] + ";");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        HBox row = new HBox(10, iconBox, textLabel);
        row.setAlignment(Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
            "-fx-background-color: " + cfg[5] + ";"
            + "-fx-background-radius: 8;"
            + "-fx-padding: 9 12 9 10;");

        return row;
    }

    // ── Divider ───────────────────────────────────────────────────────────────

    private Region buildDivider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle("-fx-background-color: #e5e7eb;");
        VBox.setMargin(divider, new Insets(16, 0, 16, 0));
        return divider;
    }
}
