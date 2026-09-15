package com.ecotwin.controller;

import com.ecotwin.AppContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Loads FXML screens, injecting (Navigator, AppContext) into each controller via a factory. */
public class Navigator {

    private final Stage stage;
    private final AppContext ctx;

    public Navigator(Stage stage, AppContext ctx) {
        this.stage = stage;
        this.ctx = ctx;
    }

    public void showLogin() {
        setSceneRoot(load("/com/ecotwin/login-view.fxml"));
    }

    public void showRegister() {
        setSceneRoot(load("/com/ecotwin/register-view.fxml"));
    }

    public void showShell() {
        setSceneRoot(load("/com/ecotwin/app-shell.fxml"));
    }

    /** Loads a view for embedding inside another screen's content area (e.g. the app shell's tabs). */
    public Parent load(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlPath));
            loader.setControllerFactory(this::createController);
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load view " + fxmlPath, e);
        }
    }

    private void setSceneRoot(Parent root) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(Navigator.class.getResource("/com/ecotwin/css/app.css").toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }

    private Object createController(Class<?> controllerClass) {
        try {
            return controllerClass.getConstructor(Navigator.class, AppContext.class).newInstance(this, ctx);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not construct controller " + controllerClass, e);
        }
    }
}
