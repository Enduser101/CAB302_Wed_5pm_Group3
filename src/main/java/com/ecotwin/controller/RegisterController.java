package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    private final Navigator nav;
    private final AppContext ctx;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    public RegisterController(Navigator nav, AppContext ctx) {
        this.nav = nav;
        this.ctx = ctx;
    }

    @FXML
    private void handleRegister() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!password.equals(confirm)) {
            showMessage("Passwords do not match");
            return;
        }

        try {
            User user = ctx.authService.register(usernameField.getText(), emailField.getText(), password);
            ctx.session.login(user);
            nav.showShell();
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage());
        }
    }

    @FXML
    private void handleLoginLink() {
        nav.showLogin();
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}
