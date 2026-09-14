package com.ecotwin;

import com.ecotwin.controller.Navigator;
import com.ecotwin.util.DatabaseConnection;
import javafx.application.Application;
import javafx.stage.Stage;

public class EcoTwinApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppContext ctx = new AppContext(DatabaseConnection.getInstance().getConnection());
        Navigator navigator = new Navigator(primaryStage, ctx);

        primaryStage.setTitle("EcoTwin");
        navigator.showLogin();
        primaryStage.show();
    }
}
