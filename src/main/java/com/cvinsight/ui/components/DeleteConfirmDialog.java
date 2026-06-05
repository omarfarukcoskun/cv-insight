package com.cvinsight.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public final class DeleteConfirmDialog {

    private DeleteConfirmDialog() {}

    /**
     * Shows a custom delete-confirmation dialog and blocks until the user
     * responds. Returns {@code true} only if the user clicked "Delete permanently".
     *
     * @param owner    parent window — used for modality and centering
     * @param filename the display name of the item being deleted (shown in the dialog)
     */
    public static boolean show(Window owner, String filename) {

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        final boolean[] confirmed = {false};

        // ── Icon area ─────────────────────────────────────────────────────────
        StackPane iconCircle = new StackPane(buildTrashIcon(32));
        iconCircle.setMinSize(68, 68);
        iconCircle.setMaxSize(68, 68);
        iconCircle.getStyleClass().add("del-dlg-icon");

        VBox topArea = new VBox(iconCircle);
        topArea.getStyleClass().add("del-dlg-top");
        topArea.setAlignment(Pos.CENTER);

        // ── Content ───────────────────────────────────────────────────────────
        Label titleLabel = new Label("Delete Resume?");
        titleLabel.getStyleClass().add("del-dlg-title");

        String display = filename.length() > 40
            ? filename.substring(0, 37) + "…"
            : filename;
        Label fileLabel = new Label(display);
        fileLabel.getStyleClass().add("del-dlg-file");

        Label descLabel = new Label(
            "This resume and all its analysis results will be\n"
            + "permanently removed. This cannot be undone.");
        descLabel.getStyleClass().add("del-dlg-desc");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);

        VBox contentArea = new VBox(10, titleLabel, fileLabel, descLabel);
        contentArea.setAlignment(Pos.CENTER);
        contentArea.setPadding(new Insets(24, 32, 0, 32));

        // ── Buttons ───────────────────────────────────────────────────────────
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("del-dlg-cancel");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);
        cancelBtn.setOnAction(e -> stage.close());

        Button deleteBtn = new Button("Delete permanently");
        deleteBtn.getStyleClass().add("del-dlg-danger");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        deleteBtn.setOnAction(e -> { confirmed[0] = true; stage.close(); });

        HBox buttonRow = new HBox(10, cancelBtn, deleteBtn);
        buttonRow.setPadding(new Insets(20, 32, 28, 32));

        // ── Root (acts as the dialog card) ────────────────────────────────────
        VBox dialogCard = new VBox(topArea, contentArea, buttonRow);
        dialogCard.getStyleClass().add("del-dlg-root");
        dialogCard.setMaxWidth(380);
        dialogCard.setPrefWidth(380);

        // Extra transparent padding around the card so the drop-shadow renders
        // beyond the card's edges without being clipped by the scene boundary.
        StackPane sceneRoot = new StackPane(dialogCard);
        sceneRoot.setPadding(new Insets(14, 18, 28, 18));
        sceneRoot.setBackground(Background.EMPTY);

        // Draggable by clicking anywhere on the dialog card
        final double[] drag = {0, 0};
        dialogCard.setOnMousePressed(e -> {
            drag[0] = e.getScreenX() - stage.getX();
            drag[1] = e.getScreenY() - stage.getY();
        });
        dialogCard.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - drag[0]);
            stage.setY(e.getScreenY() - drag[1]);
        });

        Scene scene = new Scene(sceneRoot, Color.TRANSPARENT);
        scene.getStylesheets().add(
            DeleteConfirmDialog.class
                .getResource("/com/cvinsight/ui/styles.css")
                .toExternalForm());

        stage.setScene(scene);

        // Center over the owner window after the stage has been laid out
        stage.setOnShown(ev -> {
            stage.setX(owner.getX() + (owner.getWidth()  - stage.getWidth())  / 2);
            stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
        });

        stage.showAndWait();
        return confirmed[0];
    }

    // ── Trash icon ────────────────────────────────────────────────────────────

    /**
     * Paints a red trash-can icon onto a Canvas.
     * Design grid is 36 × 36 units; scaled uniformly to {@code size}.
     */
    private static Canvas buildTrashIcon(double size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double s = size / 36.0;
        Color red   = Color.web("#dc2626");
        Color slots = Color.web("#fef2f2"); // matches dialog top-area bg

        gc.setFill(red);

        // Handle — small rounded rect sitting above the lid
        gc.fillRoundRect(12*s,  0*s, 12*s,  5*s, 3*s, 3*s);

        // Lid — full-width horizontal bar
        gc.fillRoundRect( 2*s,  7*s, 32*s,  4*s, 2*s, 2*s);

        // Body — tall main rectangle, slightly inset
        gc.fillRoundRect( 5*s, 13*s, 26*s, 21*s, 4*s, 4*s);

        // Slot lines cut into the body (three vertical grooves)
        gc.setFill(slots);
        gc.fillRoundRect(10.0*s, 16*s, 3*s, 14*s, 2*s, 2*s);
        gc.fillRoundRect(16.5*s, 16*s, 3*s, 14*s, 2*s, 2*s);
        gc.fillRoundRect(23.0*s, 16*s, 3*s, 14*s, 2*s, 2*s);

        return canvas;
    }
}
