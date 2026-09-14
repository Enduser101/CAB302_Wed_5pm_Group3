package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    private final Navigator nav;
    private final AppContext ctx;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Hyperlink registerLink;
    @FXML private Button guestButton;

    public LoginController(Navigator nav, AppContext ctx) {
        this.nav = nav;
        this.ctx = ctx;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
            return;
        }

        try {
            User user = ctx.authService.login(username, password);
            ctx.session.login(user);
            attachHouseholdIfAny(user);
            nav.showShell();
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleRegisterLink() {
        nav.showRegister();
    }

    private void attachHouseholdIfAny(User user) {
        Optional<Household> household = ctx.householdService.findActiveHouseholdForUser(user);
        household.ifPresent(h -> {
            HouseholdMembership membership = ctx.householdService.findActiveMembership(user, h).orElseThrow();
            ctx.session.enterHousehold(h, membership);
        });
    }
}
