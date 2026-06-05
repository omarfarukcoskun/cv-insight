package com.cvinsight.ui.components;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

/**
 * Circular progress-ring score display.
 *
 * Draws a two-layer arc on a Canvas (gray background + colored progress),
 * overlays the score number and "/100" label in the centre, and shows a
 * quality-badge pill below the ring.
 */
public class ScoreCard extends VBox {

    private static final int SIZE = 124; // canvas / ring diameter in px

    public ScoreCard() {
        setAlignment(Pos.CENTER);
        setSpacing(10);
    }

    public void setScore(int score) {
        getChildren().clear();

        // ── Color config based on score tier ─────────────────────────────────
        String arcColor, badgeText, badgeBg, badgeFg;
        if (score >= 80) {
            arcColor  = "#16a34a"; badgeText = "Excellent";
            badgeBg   = "#dcfce7"; badgeFg   = "#166534";
        } else if (score >= 60) {
            arcColor  = "#d97706"; badgeText = "Good";
            badgeBg   = "#fef9c3"; badgeFg   = "#854d0e";
        } else {
            arcColor  = "#dc2626"; badgeText = "Needs Work";
            badgeBg   = "#fee2e2"; badgeFg   = "#991b1b";
        }

        // ── Canvas ring ───────────────────────────────────────────────────────
        Canvas canvas = new Canvas(SIZE, SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double pad  = 14;                   // gap between canvas edge and arc centre-line
        double arcD = SIZE - 2 * pad;       // arc bounding-box dimension

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(9);

        // Background ring (full 360°)
        gc.setStroke(Color.web("#e5e7eb"));
        gc.strokeArc(pad, pad, arcD, arcD, 0, 360, ArcType.OPEN);

        // Progress arc (clockwise from 12 o'clock)
        gc.setStroke(Color.web(arcColor));
        gc.strokeArc(pad, pad, arcD, arcD, 90, -(score / 100.0) * 360, ArcType.OPEN);

        // ── Text overlay (score number + "/100") ──────────────────────────────
        Label numLabel = new Label(String.valueOf(score));
        numLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subLabel = new Label("/ 100");
        subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        VBox centerText = new VBox(1, numLabel, subLabel);
        centerText.setAlignment(Pos.CENTER);

        StackPane ringPane = new StackPane(canvas, centerText);
        ringPane.setMinSize(SIZE, SIZE);
        ringPane.setMaxSize(SIZE, SIZE);

        // ── Quality badge ─────────────────────────────────────────────────────
        Label badge = new Label(badgeText);
        badge.setStyle(
            "-fx-background-color: " + badgeBg + ";"
            + "-fx-background-radius: 20;"
            + "-fx-padding: 4 12 4 12;"
            + "-fx-text-fill: " + badgeFg + ";"
            + "-fx-font-size: 12px;"
            + "-fx-font-weight: bold;");

        getChildren().addAll(ringPane, badge);
    }
}
