package com.ecotwin.controller;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.ecotwin.AppContext;
import com.ecotwin.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** US-05: view account details. US-06: change password. US-07: sign out clears the session and returns to Login. */
public class AccountController {

    private static final DateTimeFormatter MEMBER_SINCE =
            DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZoneId.systemDefault());

    private final Navigator nav;
    private final AppContext ctx;

    @FXML private Label usernameLabel;
    @FXML private Label usernameValue;
    @FXML private Label emailValue;
    @FXML private Label memberSinceValue;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private TextField visibleCurrentPasswordField;
    @FXML private TextField visibleNewPasswordField;
    @FXML private TextField visibleConfirmPasswordField;

    @FXML private Button toggleCurrentPasswordButton;
    @FXML private Button toggleNewPasswordButton;
    @FXML private Button toggleConfirmPasswordButton;

    @FXML private Label passwordMessageLabel;
    @FXML private VBox guestSection;
    @FXML private VBox memberSection;

    public AccountController(Navigator nav, AppContext ctx) {
        this.nav = nav;
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        // Keep hidden and visible password fields synchronized
        visibleCurrentPasswordField.textProperty().bindBidirectional(currentPasswordField.textProperty());
        visibleNewPasswordField.textProperty().bindBidirectional(newPasswordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        boolean guest = ctx.session.isGuest();
        guestSection.setVisible(guest);   guestSection.setManaged(guest);
        memberSection.setVisible(!guest); memberSection.setManaged(!guest);
        if (guest) {
            usernameLabel.setText("Browsing as a guest");
            return;                                        //check what page to display on whether the user is a guest or not
        }
        User user = ctx.session.getCurrentUser();
        usernameLabel.setText("Signed in as " + user.getUsername());
        usernameValue.setText(user.getUsername());
        emailValue.setText(user.getEmail());
        memberSinceValue.setText(formatCreatedAt(user.getCreatedAt()));
    }

    private String formatCreatedAt(String createdAt) {
        // created_at is stored as an ISO instant, fall back to the raw value if it won't parse
        try {
            return MEMBER_SINCE.format(Instant.parse(createdAt));
        } catch (Exception e) {
            return createdAt;
        }
    }

    // Show or hide current password
    @FXML
    private void handleToggleCurrentPassword() {
        boolean showPassword = !visibleCurrentPasswordField.isVisible();

        visibleCurrentPasswordField.setVisible(showPassword);
        visibleCurrentPasswordField.setManaged(showPassword);

        currentPasswordField.setVisible(!showPassword);
        currentPasswordField.setManaged(!showPassword);

        toggleCurrentPasswordButton.setText(showPassword ? "◉" : "◎");
    }

    // Show or hide new password
    @FXML
    private void handleToggleNewPassword() {
        boolean showPassword = !visibleNewPasswordField.isVisible();

        visibleNewPasswordField.setVisible(showPassword);
        visibleNewPasswordField.setManaged(showPassword);

        newPasswordField.setVisible(!showPassword);
        newPasswordField.setManaged(!showPassword);

        toggleNewPasswordButton.setText(showPassword ? "◉" : "◎");
    }

    // Show or hide confirm password
    @FXML
    private void handleToggleConfirmPassword() {
        boolean showPassword = !visibleConfirmPasswordField.isVisible();

        visibleConfirmPasswordField.setVisible(showPassword);
        visibleConfirmPasswordField.setManaged(showPassword);

        confirmPasswordField.setVisible(!showPassword);
        confirmPasswordField.setManaged(!showPassword);

        toggleConfirmPasswordButton.setText(showPassword ? "◉" : "◎");
    }

    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText();
        String updated = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!updated.equals(confirm)) {
            showPasswordMessage("Passwords do not match");
            return;
        }

        try {
            ctx.authService.changePassword(ctx.session.getCurrentUser().getUsername(), current, updated);
            showPasswordMessage("Password updated");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            // Hide all password fields after a successful update
            visibleCurrentPasswordField.setVisible(false);
            visibleCurrentPasswordField.setManaged(false);
            currentPasswordField.setVisible(true);
            currentPasswordField.setManaged(true);
            toggleCurrentPasswordButton.setText("◎");

            visibleNewPasswordField.setVisible(false);
            visibleNewPasswordField.setManaged(false);
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            toggleNewPasswordButton.setText("◎");

            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            toggleConfirmPasswordButton.setText("◎");

        } catch (IllegalArgumentException e) {
            showPasswordMessage(e.getMessage());
        }
    }

    private void showPasswordMessage(String message) {
        passwordMessageLabel.setText(message);
        passwordMessageLabel.setVisible(true);
        passwordMessageLabel.setManaged(true);
    }

    @FXML
    private void handleSignOut() {
        ctx.session.logout();
        nav.showLogin();
    }

    @FXML
    private void guestHandleSignIn() {
        ctx.session.logout();
        nav.showLogin();
    }

    @FXML
    private void guestHandleCreateAccount() {
        ctx.session.logout();
        nav.showRegister();
    }
}