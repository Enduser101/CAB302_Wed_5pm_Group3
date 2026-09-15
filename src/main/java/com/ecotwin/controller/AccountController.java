package com.ecotwin.controller;

import com.ecotwin.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** US-07: sign out clears the session and returns to Login. */
public class AccountController {

    private final Navigator nav;
    private final AppContext ctx;

    @FXML private Label usernameLabel;

    public AccountController(Navigator nav, AppContext ctx) {
        this.nav = nav;
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        usernameLabel.setText("Signed in as " + ctx.session.getCurrentUser().getUsername());
    }

    @FXML
    private void handleSignOut() {
        ctx.session.logout();
        nav.showLogin();
    }
}
