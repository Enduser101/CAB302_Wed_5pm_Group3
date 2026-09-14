package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** US-08 (create a household) and US-09 (join a household) - plus a minimal read of the
 *  resulting household once the user has one. Member management (US-11+) is out of scope here. */
public class HouseholdController {

    private static final java.util.List<String> AUSTRALIAN_STATES =
        java.util.List.of("ACT", "NSW", "NT", "QLD", "SA", "TAS", "VIC", "WA");

    private final Navigator nav;
    private final AppContext ctx;

    @FXML private VBox setupSection;
    @FXML private VBox summarySection;

    @FXML private TextField householdNameField;
    @FXML private TextField occupantsField;
    @FXML private ComboBox<String> stateCombo;
    @FXML private Label createErrorLabel;

    @FXML private TextField joinCodeField;
    @FXML private Label joinErrorLabel;

    @FXML private Label summaryNameLabel;
    @FXML private Label summaryRoleLabel;
    @FXML private Label summaryJoinCodeLabel;

    public HouseholdController(Navigator nav, AppContext ctx) {
        this.nav = nav;
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        stateCombo.getItems().addAll(AUSTRALIAN_STATES);
        refresh();
    }

    @FXML
    private void handleCreateHousehold() {
        try {
            int occupants = Integer.parseInt(occupantsField.getText().trim());
            Household household = ctx.householdService.createHousehold(
                ctx.session.getCurrentUser(), householdNameField.getText(), occupants, null, stateCombo.getValue());
            enterHousehold(ctx.session.getCurrentUser(), household);
            hideError(createErrorLabel);
            refresh();
        } catch (NumberFormatException e) {
            showError(createErrorLabel, "Number of occupants must be a number");
        } catch (IllegalArgumentException e) {
            showError(createErrorLabel, e.getMessage());
        }
    }

    @FXML
    private void handleJoinHousehold() {
        try {
            User user = ctx.session.getCurrentUser();
            Household household = ctx.householdService.joinHousehold(user, joinCodeField.getText());
            enterHousehold(user, household);
            hideError(joinErrorLabel);
            refresh();
        } catch (IllegalArgumentException e) {
            showError(joinErrorLabel, e.getMessage());
        }
    }

    private void enterHousehold(User user, Household household) {
        HouseholdMembership membership = ctx.householdService.findActiveMembership(user, household).orElseThrow();
        ctx.session.enterHousehold(household, membership);
    }

    private void refresh() {
        boolean hasHousehold = ctx.session.hasActiveHousehold();
        setupSection.setVisible(!hasHousehold);
        setupSection.setManaged(!hasHousehold);
        summarySection.setVisible(hasHousehold);
        summarySection.setManaged(hasHousehold);

        if (hasHousehold) {
            Household household = ctx.session.getCurrentHousehold();
            boolean admin = ctx.session.isAdmin();
            summaryNameLabel.setText(household.getName());
            summaryRoleLabel.setText(admin ? "Administrator" : "Member");
            summaryJoinCodeLabel.setText("Join code: " + household.getJoinCode());
            summaryJoinCodeLabel.setVisible(admin);
            summaryJoinCodeLabel.setManaged(admin);
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
