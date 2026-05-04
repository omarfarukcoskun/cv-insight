package com.cvinsight.app;

import com.cvinsight.db.DatabaseManager;
import com.cvinsight.db.DataSeeder;
import com.cvinsight.ui.SceneManager;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseManager.getInstance(); // initialise DB + create tables on first run
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not open the database");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            javafx.application.Platform.exit();
            return;
        }

        DataSeeder.seedIfEmpty(); // seed example CVs on first run

        SceneManager.init(primaryStage);
        primaryStage.setTitle("CV Insight");
        primaryStage.setWidth(960);
        primaryStage.setHeight(680);
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(600);

        SceneManager.switchTo("login.fxml");
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
