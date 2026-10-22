package com.ecotwin;

import com.ecotwin.controller.Navigator;
import com.ecotwin.util.DatabaseConnection;
import com.ecotwin.util.DemoAccountSeeder;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;

public class EcoTwinApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        AppContext ctx = new AppContext(connection);
        new DemoAccountSeeder(connection, ctx).seedIfMissing(); // #115: log in as "Cab 302" / "Testuser1"
        Navigator navigator = new Navigator(primaryStage, ctx);

        primaryStage.setTitle("EcoTwin");
        navigator.showLogin();
        primaryStage.show();
    }
}
