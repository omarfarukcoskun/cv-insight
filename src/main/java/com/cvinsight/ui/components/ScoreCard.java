package com.cvinsight.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A circular score display showing a large number over a coloured background.
 *
 * Usage:
 *   ScoreCard card = new ScoreCard();
 *   card.setScore(78);
 *   someContainer.getChildren().add(card);
 */
public class ScoreCard extends VBox {

    private final Label     numberLabel = new Label("--");
    private final Label     maxLabel    = new Label("/ 100");
    private final StackPane circle;                          // FIX: field so setScore() can reach it

    public ScoreCard() {
        setAlignment(Pos.CENTER);
        setSpacing(8);

        numberLabel.getStyleClass().add("score-number");
        maxLabel.getStyleClass().add("score-max");

        VBox inner = new VBox(2, numberLabel, maxLabel);
        inner.setAlignment(Pos.CENTER);

        circle = new StackPane(inner);
        circle.getStyleClass().add("score-circle");
        circle.setAlignment(Pos.CENTER);

        getChildren().add(circle);
    }

    public void setScore(int score) {
        numberLabel.setText(String.valueOf(score));

        // Colour the circle based on score range
        String colour;
        if (score >= 80)      colour = "#16a34a"; // green
        else if (score >= 60) colour = "#d97706"; // amber
        else                  colour = "#dc2626"; // red

        // FIX: apply to circle, not the outer VBox
        circle.setStyle(
            "-fx-background-color: " + colour + ";" +
            "-fx-background-radius: 60;" +
            "-fx-min-width: 120; -fx-min-height: 120;" +
            "-fx-max-width: 120; -fx-max-height: 120;"
        );
    }
}
