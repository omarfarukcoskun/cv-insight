package com.cvinsight.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Central scene-switching utility.
 *
 * Usage (switch with no data):
 *   SceneManager.switchTo("dashboard.fxml");
 *
 * Usage (switch and configure the new controller):
 *   FXMLLoader loader = SceneManager.switchTo("analysis.fxml");
 *   AnalysisController ctrl = loader.getController();
 *   ctrl.initWithCV(cv);
 */
public class SceneManager {

    private static Stage stage;

    private SceneManager() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /**
     * Loads the given FXML file and replaces the current scene root.
     * Returns the FXMLLoader so callers can access the controller for data passing.
     */
    public static FXMLLoader switchTo(String fxmlFile) {
        try {
            URL resource = SceneManager.class.getResource("/com/cvinsight/ui/" + fxmlFile);
            if (resource == null) {
                throw new IllegalArgumentException("FXML not found: /com/cvinsight/ui/" + fxmlFile);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }
            return loader;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load screen: " + fxmlFile, e);
        }
    }

    public static Stage getStage() {
        return stage;
    }
}
