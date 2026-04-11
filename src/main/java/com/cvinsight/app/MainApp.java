package com.cvinsight.app;

import com.cvinsight.db.DatabaseManager;
import com.cvinsight.ui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance(); // initialise DB + create tables on first run

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
