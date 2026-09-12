package com.ecotwin;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class EcoTwinApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                EcoTwinApplication.class.getResource("/com/ecotwin/login-view.fxml")
        );
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("EcoTwin");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
