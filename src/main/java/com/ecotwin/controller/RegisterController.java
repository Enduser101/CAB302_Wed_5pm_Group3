package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML private TextField visiblePasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private Button togglePasswordButton;
    @FXML private Button toggleConfirmPasswordButton;

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

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    @FXML
    private void handleTogglePassword() {
        boolean showPassword = !visiblePasswordField.isVisible();

        visiblePasswordField.setVisible(showPassword);
        visiblePasswordField.setManaged(showPassword);

        passwordField.setVisible(!showPassword);
        passwordField.setManaged(!showPassword);

        togglePasswordButton.setText(showPassword ? "◉" : "◎");
    }

    @FXML
    private void handleToggleConfirmPassword() {
        boolean showPassword = !visibleConfirmPasswordField.isVisible();

        visibleConfirmPasswordField.setVisible(showPassword);
        visibleConfirmPasswordField.setManaged(showPassword);

        confirmPasswordField.setVisible(!showPassword);
        confirmPasswordField.setManaged(!showPassword);

        toggleConfirmPasswordButton.setText(showPassword ? "◉" : "◎");
    }
}
