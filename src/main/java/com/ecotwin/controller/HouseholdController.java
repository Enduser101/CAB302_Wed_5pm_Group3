package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.ecotwin.model.ActivityLogEntry;

import java.util.Optional;

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
    private VBox householdAdminButtons;

    // US-33: error message when renaming a household
    @FXML
    private Label renameHouseholdErrorLabel;

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
                // US-36: arrange administrator buttons into separate rows
                HBox adminButtonRow = new HBox(10);

                Button activityButton = new Button("Activity History");
                activityButton.getStyleClass().add("btn-secondary");
                activityButton.setMaxWidth(Double.MAX_VALUE);
                activityButton.setOnAction(event -> handleShowActivityHistory());

                // US-33: rename a household
                Button renameButton = new Button("Rename Household");
                renameButton.getStyleClass().add("btn-secondary");
                renameButton.setMaxWidth(Double.MAX_VALUE);
                renameButton.setOnAction(event -> handleRenameHousehold());

                // US-35: transfer administrator rights
                Button transferButton = new Button("Transfer Admin");
                transferButton.getStyleClass().add("btn-secondary");
                transferButton.setMaxWidth(Double.MAX_VALUE);
                transferButton.setOnAction(event -> handleTransferAdmin());

                // US-33: change the household join code
                Button joinCodeButton = new Button("Generate New Join Code");
                joinCodeButton.getStyleClass().add("btn-secondary");
                joinCodeButton.setMaxWidth(Double.MAX_VALUE);
                joinCodeButton.setOnAction(event -> handleChangeJoinCode());

                adminButtonRow.getChildren().addAll(
                        activityButton,
                        renameButton,
                        transferButton,
                        joinCodeButton
                );


                // US-36: delete a household
                Button deleteHouseholdButton = new Button("Delete Household");
                deleteHouseholdButton.getStyleClass().add("btn-danger");
                deleteHouseholdButton.setOnAction(event -> handleDeleteHousehold());

                HBox deleteButtonRow = new HBox(10);
                deleteButtonRow.getChildren().add(deleteHouseholdButton);

                householdAdminButtons.getChildren().addAll(
                        adminButtonRow,
                        deleteButtonRow
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

    // US-12: view household activity history, newest first
    // US-34: admins can edit and delete activity history entries
    private void handleShowActivityHistory() {
        Household household = ctx.session.getCurrentHousehold();
        User user = ctx.session.getCurrentUser();

        // US-34: only show editing controls to the administrator
        boolean isAdmin = ctx.householdService.findActiveMembership(user, household)
                .map(membership ->
                        membership.getRole() == HouseholdMembership.Role.ADMIN)
                .orElse(false);

        VBox list = new VBox(8);
        list.setPadding(new javafx.geometry.Insets(12));

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(560, 360);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(household.getName() + ": Activity History");
        dialog.setHeaderText("Newest first");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // US-34: populate the history when the dialog opens
        populateActivityHistory(list, household, user, isAdmin);

        dialog.showAndWait();
    }

    // US-34: refresh the existing activity history list
    private void populateActivityHistory(
            VBox list, Household household, User user, boolean isAdmin) {

        list.getChildren().clear();

        // Get the latest history after every edit or deletion
        java.util.List<ActivityLogEntry> history =
                ctx.householdService.getHistory(household);

        if (history.isEmpty()) {
            list.getChildren().add(new Label("No activity yet."));
        }

        for (ActivityLogEntry entry : history) {
            Label line = new Label(
                    formatTime(entry.getCreatedAt()) + "   " + entry.getMessage());
            line.setWrapText(true);
            line.setPrefWidth(350);
            line.setMinWidth(350);
            line.setMaxWidth(350);

            // US-34: prevent changing entries that have already been modified or deleted
            boolean isAuditEntry =
                    entry.getMessage().contains(" edited activity entry #")
                            || entry.getMessage().contains(" deleted activity entry #");

            boolean alreadyChanged = history.stream().anyMatch(item ->
                    item.getMessage().contains("edited activity entry #" + entry.getId() + ".")
                            || item.getMessage().contains("deleted activity entry #" + entry.getId() + "."));

            boolean canModify = !isAuditEntry && !alreadyChanged;

            if (isAdmin && canModify) {
                // US-34: edit an activity history entry
                Button editButton = new Button("Edit");
                editButton.setPrefSize(75, 30);
                editButton.setMinSize(75, 30);
                editButton.setMaxSize(75, 30);
                editButton.setOnAction(event -> {
                    javafx.scene.control.TextInputDialog editDialog =
                            new javafx.scene.control.TextInputDialog(entry.getMessage());

                    editDialog.setTitle("Edit Activity");
                    editDialog.setHeaderText("Correct activity history");
                    editDialog.setContentText("Activity message:");

                    editDialog.showAndWait().ifPresent(newMessage -> {
                        try {
                            ctx.householdService.editHistoryEntry(
                                    user, household, entry.getId(), newMessage);

                            // US-34: refresh the same dialog's list
                            populateActivityHistory(list, household, user, isAdmin);
                        } catch (IllegalArgumentException e) {
                            showHistoryError(e.getMessage());
                        }
                    });
                });

                // US-34: delete an activity history entry
                Button deleteButton = new Button("Delete");
                deleteButton.getStyleClass().add("btn-danger");
                deleteButton.setOnAction(event -> {
                    javafx.scene.control.Alert confirmation =
                            new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.CONFIRMATION);

                    confirmation.setTitle("Delete Activity");
                    confirmation.setHeaderText("Delete this activity entry?");
                    confirmation.setContentText(
                            "The original message will be recorded in the activity history.");

                    confirmation.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                ctx.householdService.deleteHistoryEntry(
                                        user, household, entry.getId());

                                // US-34: refresh the same dialog's list
                                populateActivityHistory(list, household, user, isAdmin);
                            } catch (IllegalArgumentException e) {
                                showHistoryError(e.getMessage());
                            }
                        }
                    });
                });

                HBox entryRow = new HBox(8, line, editButton, deleteButton);
                entryRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                HBox.setHgrow(line, javafx.scene.layout.Priority.ALWAYS);
                list.getChildren().add(entryRow);
            } else {
                list.getChildren().add(line);
            }
        }
    }

    // US-34: display activity history errors
    private void showHistoryError(String message) {
        javafx.scene.control.Alert error =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);

        error.setTitle("Activity History");
        error.setHeaderText("Could not update activity history");
        error.setContentText(message);
        error.showAndWait();
    }


    // US 12: show times in the computer's own time zone
    private static String formatTime(String createdAt) {
        try {
            return java.time.Instant.parse(createdAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a"));
        } catch (java.time.format.DateTimeParseException e) {
            return createdAt;
        }
    }
    // US-33: rename a household
    private void handleRenameHousehold() {
        TextInputDialog dialog = new TextInputDialog(
                ctx.session.getCurrentHousehold().getName()
        );

        dialog.setTitle("Rename Household");
        dialog.setHeaderText("Rename your household");
        dialog.setContentText("New household name:");

        dialog.showAndWait().ifPresent(newName -> {
            try {
                User admin = ctx.session.getCurrentUser();
                Household household = ctx.session.getCurrentHousehold();

                ctx.householdService.renameHousehold(admin, household, newName);

                hideError(renameHouseholdErrorLabel);

                ctx.householdService.findActiveHouseholdForUser(admin)
                        .ifPresent(updatedHousehold ->
                                ctx.session.enterHousehold(
                                        updatedHousehold,
                                        ctx.householdService.findActiveMembership(admin, updatedHousehold).orElseThrow()
                                )
                        );

                refresh();
            } catch (IllegalArgumentException e) {
                showError(renameHouseholdErrorLabel, e.getMessage());
            }
        });
    }

    // US-35: transfer administrator rights
    private void handleTransferAdmin() {
        User admin = ctx.session.getCurrentUser();
        Household household = ctx.session.getCurrentHousehold();

        java.util.List<User> members = ctx.householdService.findActiveMembers(household)
                .stream()
                .filter(member -> member.getId() != admin.getId())
                .toList();

        if (members.isEmpty()) {
            showError(renameHouseholdErrorLabel, "There are no other active members to transfer administrator rights to");
            return;
        }

        java.util.List<String> memberNames = members.stream()
                .map(member -> member.getDisplayName() != null
                        ? member.getDisplayName()
                        : member.getUsername())
                .toList();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(memberNames.get(0), memberNames);

        dialog.setTitle("Transfer Admin");
        dialog.setHeaderText("Transfer administrator rights");
        dialog.setContentText("Choose a member:");

        dialog.showAndWait().ifPresent(selectedName -> {
            User newAdmin = members.stream()
                    .filter(member -> {
                        String name = member.getDisplayName() != null
                                ? member.getDisplayName()
                                : member.getUsername();
                        return name.equals(selectedName);
                    })
                    .findFirst()
                    .orElseThrow();

            try {
                ctx.householdService.transferAdmin(admin, household, newAdmin);

                // Refresh the household membership so the current user is now a member
                ctx.householdService.findActiveHouseholdForUser(admin)
                        .ifPresent(updatedHousehold ->
                                ctx.session.enterHousehold(
                                        updatedHousehold,
                                        ctx.householdService.findActiveMembership(admin, updatedHousehold).orElseThrow()
                                )
                        );

                refresh();
            } catch (IllegalArgumentException e) {
                showError(renameHouseholdErrorLabel, e.getMessage());
            }
        });
    }

    // US-33: change the household join code
    private void handleChangeJoinCode() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Generate New Join Code");
        confirmation.setHeaderText("Generate a new household join code?");
        confirmation.setContentText(
                "The old join code will no longer work."
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    User admin = ctx.session.getCurrentUser();
                    Household household = ctx.session.getCurrentHousehold();

                    ctx.householdService.changeJoinCode(admin, household);

                    // Refresh the household so the new join code is displayed
                    ctx.householdService.findActiveHouseholdForUser(admin)
                            .ifPresent(updatedHousehold ->
                                    ctx.session.enterHousehold(
                                            updatedHousehold,
                                            ctx.householdService.findActiveMembership(
                                                    admin,
                                                    updatedHousehold
                                            ).orElseThrow()
                                    )
                            );

                    refresh();
                } catch (IllegalArgumentException e) {
                    showError(renameHouseholdErrorLabel, e.getMessage());
                }
            }
        });
    }

    // US-36: delete a household
    @FXML
    private void handleDeleteHousehold() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Household");
        confirmation.setHeaderText("Permanently delete this household?");
        confirmation.setContentText(
                "All household memberships and activity history will be deleted. This cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                User user = ctx.session.getCurrentUser();
                Household household = ctx.session.getCurrentHousehold();

                ctx.householdService.deleteHousehold(user, household);
                ctx.session.leaveHousehold();
                refresh();
            } catch (IllegalArgumentException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Delete Household");
                error.setHeaderText("Could not delete household");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

}