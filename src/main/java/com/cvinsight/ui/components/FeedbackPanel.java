package com.cvinsight.ui.components;

import com.cvinsight.model.Feedback;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Displays feedback from an AI analysis in three colour-coded sections:
 * Strengths (green), Weaknesses (red), Suggestions (blue).
 *
 * Usage:
 *   FeedbackPanel panel = new FeedbackPanel();
 *   panel.populate(feedback);
 *   someContainer.getChildren().add(panel);
 */
public class FeedbackPanel extends VBox {

    public FeedbackPanel() {
        setSpacing(20);
    }

    public void populate(Feedback feedback) {
        getChildren().clear();
        getChildren().addAll(
            buildSection("Strengths",   feedback.getStrengths(),   "strength-item"),
            buildSection("Weaknesses",  feedback.getWeaknesses(),  "weakness-item"),
            buildSection("Suggestions", feedback.getSuggestions(), "suggestion-item")
        );
    }

    private VBox buildSection(String title, List<String> items, String itemStyleClass) {
        VBox section = new VBox(8);

        Label heading = new Label(title);
        heading.getStyleClass().add("section-heading");
        section.getChildren().add(heading);

        if (items.isEmpty()) {
            Label empty = new Label("None");
            empty.setStyle("-fx-text-fill: #9ca3af;");
            section.getChildren().add(empty);
        } else {
            for (String item : items) {
                Label label = new Label("• " + item);
                label.getStyleClass().add(itemStyleClass);
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
                section.getChildren().add(label);
            }
        }
        return section;
    }
}
