package com.cvinsight.ui.components;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PDFPreviewDialog {

    private static final float[]  ZOOM_DPIS   = { 48f, 72f, 96f, 120f, 144f, 192f };
    private static final String[] ZOOM_LABELS = { "50%", "75%", "100%", "125%", "150%", "200%" };
    private static final int DEFAULT_ZOOM = 1;

    @FunctionalInterface
    interface PDFLoader { PDDocument load() throws IOException; }

    // ── Public entry points ───────────────────────────────────────────────────

    /** Load from a local file path (used for user-uploaded PDFs). */
    public static void show(Window owner, String filePath, String filename) {
        File file = new File(filePath);
        showInternal(owner, filename, () -> {
            if (!file.exists() || !file.canRead()) {
                throw new IOException("File not found: " + file.getAbsolutePath());
            }
            return Loader.loadPDF(file);
        });
    }

    /** Load from a pre-read byte array (used for bundled resource PDFs). */
    public static void showFromBytes(Window owner, byte[] bytes, String title) {
        showInternal(owner, title,
            () -> Loader.loadPDF(new RandomAccessReadBuffer(bytes)));
    }

    // ── Shared dialog builder ─────────────────────────────────────────────────

    private static void showInternal(Window owner, String title, PDFLoader loader) {

        int[]         pageIdx   = { 0 };
        int[]         zoomIdx   = { DEFAULT_ZOOM };
        int[]         pageCount = { 0 };
        PDDocument[]  doc       = { null };
        PDFRenderer[] rend      = { null };

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(title);
        stage.setMinWidth(600);
        stage.setMinHeight(480);
        stage.setWidth(880);
        stage.setHeight(720);

        Label pageLabel = new Label("—");
        Label zoomLabel = new Label(ZOOM_LABELS[DEFAULT_ZOOM]);
        pageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; "
            + "-fx-min-width: 72; -fx-alignment: center;");
        zoomLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; "
            + "-fx-min-width: 44; -fx-alignment: center;");

        ImageView pageView = new ImageView();
        pageView.setPreserveRatio(true);
        pageView.setSmooth(true);

        StackPane imgWrapper = new StackPane(pageView);
        imgWrapper.setAlignment(Pos.CENTER);
        imgWrapper.setStyle("-fx-background-color: #475569; -fx-padding: 28;");

        ScrollPane scroll = new ScrollPane(imgWrapper);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setStyle("-fx-background-color: #475569; -fx-background: #475569;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Label loadingLbl = new Label("Loading PDF…");
        loadingLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: white;");
        StackPane loadingPane = new StackPane(loadingLbl);
        loadingPane.setStyle("-fx-background-color: #475569;");
        VBox.setVgrow(loadingPane, Priority.ALWAYS);

        Label errLbl = new Label();
        errLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
        errLbl.setWrapText(true);
        errLbl.setMaxWidth(460);
        Button retryBtn = new Button("Retry");
        retryBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-background-radius: 8; "
            + "-fx-padding: 9 22 9 22; -fx-cursor: hand;");
        VBox errPane = new VBox(14, errLbl, retryBtn);
        errPane.setAlignment(Pos.CENTER);
        errPane.setStyle("-fx-background-color: #f8fafc; -fx-padding: 56;");
        VBox.setVgrow(errPane, Priority.ALWAYS);

        Button prevBtn    = navBtn("◀");
        Button nextBtn    = navBtn("▶");
        Button zoomOutBtn = navBtn("−");
        Button zoomInBtn  = navBtn("+");

        Button openExtBtn = new Button("Open externally");
        openExtBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; "
            + "-fx-font-weight: bold; -fx-font-size: 12px; "
            + "-fx-background-radius: 7; -fx-padding: 6 14 6 14; -fx-cursor: hand;");

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; "
            + "-fx-font-weight: bold; -fx-font-size: 13px; "
            + "-fx-background-radius: 7; -fx-padding: 6 12 6 12; -fx-cursor: hand;");

        Runnable render = () -> {
            if (doc[0] == null) return;
            try {
                BufferedImage bi = rend[0].renderImageWithDPI(pageIdx[0], ZOOM_DPIS[zoomIdx[0]]);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bi, "png", baos);
                Image img = new Image(new ByteArrayInputStream(baos.toByteArray()));
                Platform.runLater(() -> {
                    pageView.setImage(img);
                    pageLabel.setText((pageIdx[0] + 1) + " / " + pageCount[0]);
                    zoomLabel.setText(ZOOM_LABELS[zoomIdx[0]]);
                    prevBtn.setDisable(pageIdx[0] == 0);
                    nextBtn.setDisable(pageIdx[0] == pageCount[0] - 1);
                    zoomOutBtn.setDisable(zoomIdx[0] == 0);
                    zoomInBtn.setDisable(zoomIdx[0] == ZOOM_DPIS.length - 1);
                });
            } catch (IOException ex) {
                Platform.runLater(() -> errLbl.setText("Failed to render page: " + ex.getMessage()));
            }
        };

        Runnable[] loadPdf = { null };
        loadPdf[0] = () -> {
            try {
                PDDocument loaded = loader.load();
                doc[0]       = loaded;
                rend[0]      = new PDFRenderer(loaded);
                pageCount[0] = loaded.getNumberOfPages();

                BufferedImage bi = rend[0].renderImageWithDPI(0, ZOOM_DPIS[zoomIdx[0]]);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bi, "png", baos);
                Image img = new Image(new ByteArrayInputStream(baos.toByteArray()));

                Platform.runLater(() -> {
                    pageView.setImage(img);
                    pageLabel.setText("1 / " + pageCount[0]);
                    zoomLabel.setText(ZOOM_LABELS[zoomIdx[0]]);
                    prevBtn.setDisable(true);
                    nextBtn.setDisable(pageCount[0] <= 1);
                    zoomOutBtn.setDisable(zoomIdx[0] == 0);
                    zoomInBtn.setDisable(zoomIdx[0] == ZOOM_DPIS.length - 1);
                    ((VBox) stage.getScene().getRoot()).getChildren().set(1, scroll);
                });
            } catch (IOException ex) {
                final Runnable retryAction = loadPdf[0];
                Platform.runLater(() -> {
                    errLbl.setText("Could not open PDF:\n" + ex.getMessage());
                    retryBtn.setVisible(true);
                    retryBtn.setOnAction(ev -> {
                        ((VBox) stage.getScene().getRoot()).getChildren().set(1, loadingPane);
                        Thread.ofVirtual().start(retryAction);
                    });
                    ((VBox) stage.getScene().getRoot()).getChildren().set(1, errPane);
                });
            }
        };

        prevBtn.setOnAction(e -> {
            if (pageIdx[0] > 0) { pageIdx[0]--; Thread.ofVirtual().start(render); }
        });
        nextBtn.setOnAction(e -> {
            if (pageIdx[0] < pageCount[0] - 1) { pageIdx[0]++; Thread.ofVirtual().start(render); }
        });
        zoomOutBtn.setOnAction(e -> {
            if (zoomIdx[0] > 0) { zoomIdx[0]--; Thread.ofVirtual().start(render); }
        });
        zoomInBtn.setOnAction(e -> {
            if (zoomIdx[0] < ZOOM_DPIS.length - 1) { zoomIdx[0]++; Thread.ofVirtual().start(render); }
        });
        openExtBtn.setOnAction(e -> { /* no-op for resource PDFs */ });
        closeBtn.setOnAction(e -> stage.close());

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        titleLbl.setMaxWidth(280);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Region sep = new Region();
        sep.setMinWidth(1); sep.setMaxWidth(1); sep.setMinHeight(18);
        sep.setStyle("-fx-background-color: #e5e7eb;");

        HBox toolbar = new HBox(10, titleLbl, spacer,
            prevBtn, pageLabel, nextBtn, sep,
            zoomOutBtn, zoomLabel, zoomInBtn,
            openExtBtn, closeBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-padding: 10 16 10 16; "
            + "-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        VBox root = new VBox(toolbar, loadingPane);
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(ev -> closeDoc(doc));
        stage.show();
        Thread.ofVirtual().start(loadPdf[0]);
    }

    private static Button navBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #374151; "
            + "-fx-font-size: 13px; -fx-font-weight: bold; "
            + "-fx-background-radius: 6; -fx-padding: 5 11 5 11; -fx-cursor: hand;");
        return btn;
    }

    private static void closeDoc(PDDocument[] doc) {
        if (doc[0] != null) {
            try { doc[0].close(); } catch (IOException ignored) {}
            doc[0] = null;
        }
    }
}
