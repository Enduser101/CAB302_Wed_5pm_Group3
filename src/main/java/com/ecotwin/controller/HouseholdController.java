package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** US-08 (create a household), US-09 (join a household) and US-10 (delete a household) - plus a minimal read of the
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

    // US-10: leave a household
    @FXML private Label leaveErrorLabel;
    @FXML private Button leaveButton;

    // US-11: view current household members
    @FXML private VBox membersListView;

    // US-32: error message when removing a member
    @FXML
    private Label removeMemberErrorLabel;

    // US-32: household administrator actions
    @FXML
    private HBox householdAdminButtons;

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

            // US-10: only members can leave the household
            leaveButton.setVisible(!admin);
            leaveButton.setManaged(!admin);

            summaryNameLabel.setText(household.getName());
            summaryJoinCodeLabel.setText("Join code: " + household.getJoinCode());
            summaryJoinCodeLabel.setVisible(admin);
            summaryJoinCodeLabel.setManaged(admin);

            // US-11: display current household members
            membersListView.getChildren().clear();

            java.util.List<User> members = ctx.householdService.findActiveMembers(household);

            for (int i = 0; i < members.size(); i++) {
                User member = members.get(i);

                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(16);

                // US-32: keep all member rows the same height
                row.setMinHeight(45);
                row.setPrefHeight(45);
                row.setMaxHeight(45);

                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: #28543d;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 12 20 12 20;"
                );

                javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(
                        member.getDisplayName() != null ? member.getDisplayName() : member.getUsername()
                );
                nameLabel.setStyle("-fx-font-size: 20px;");

                row.getChildren().add(nameLabel);

                // US-11: show administrator beside the first member
                if (i == 0) {
                    javafx.scene.control.Label adminLabel =
                            new javafx.scene.control.Label("Administrator");
                    adminLabel.setStyle("-fx-font-size: 16px;");
                    row.getChildren().add(adminLabel);
                }

                // US-11: show which member is the current user
                if (member.getId() == ctx.session.getCurrentUser().getId()) {
                    javafx.scene.control.Label youLabel =
                            new javafx.scene.control.Label("(you)");
                    youLabel.setStyle("-fx-font-size: 16px;");
                    row.getChildren().add(youLabel);
                }

                // US-32: show remove button for the administrator
                if (admin && i != 0) {
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    row.getChildren().add(spacer);

                    javafx.scene.control.Button removeButton =
                            new javafx.scene.control.Button("Remove");
                    removeButton.getStyleClass().add("btn-danger");
                    removeButton.setOnAction(event -> handleRemoveMember(member));
                    row.getChildren().add(removeButton);
                }

                membersListView.getChildren().add(row);
            }

            // US-32: household administrator actions
            householdAdminButtons.getChildren().clear();

            if (admin) {
                Button activityButton = new Button("Activity History");
                activityButton.getStyleClass().add("edit-button");

                Button renameButton = new Button("Rename Household");
                renameButton.getStyleClass().add("edit-button");

                Button transferButton = new Button("Transfer Admin");
                transferButton.getStyleClass().add("edit-button");

                Button joinCodeButton = new Button("Generate New Join Code");
                joinCodeButton.getStyleClass().add("edit-button");

                householdAdminButtons.getChildren().addAll(
                        activityButton,
                        renameButton,
                        transferButton,
                        joinCodeButton
                );
            }
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

    // US-10: leave a household
    @FXML
    private void handleLeaveHousehold() {
        try {
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            ctx.householdService.leaveHousehold(user, household);
            ctx.session.leaveHousehold();
            hideError(leaveErrorLabel);
            refresh();
        } catch (IllegalArgumentException e) {
            showError(leaveErrorLabel, e.getMessage());
        }
    }

    // US-32: remove a household member
    private void handleRemoveMember(User member) {
        try {
            User admin = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            ctx.householdService.removeMember(admin, household, member);

            hideError(removeMemberErrorLabel);
            refresh();
        } catch (IllegalArgumentException e) {
            showError(removeMemberErrorLabel, e.getMessage());
        }
    }
}