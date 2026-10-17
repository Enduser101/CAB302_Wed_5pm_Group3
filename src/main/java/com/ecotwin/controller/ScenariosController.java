package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.SavedScenario;
import com.ecotwin.model.ScenarioComparison;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

/**
 * US-27: Create a temporary sustainability scenario.
 *
 * Scenario values are copied from current household data and can be changed
 * without changing the household's real resource records.
 *
 * US-28: the scenario's score is shown beside the current score.
 * US-29/30/31: scenarios can be saved, reopened and deleted.
 */
public class ScenariosController {

    private final AppContext ctx;

    private SustainabilityScenario currentValues;
    private SustainabilityScenario scenario;

    @FXML private VBox noHouseholdSection;
    @FXML private VBox scenarioSection;

    @FXML private TextField energyKwhField;
    @FXML private TextField solarGenerationKwhField;
    @FXML private TextField waterLitresField;
    @FXML private TextField generalWasteKgField;
    @FXML private TextField recycledWasteKgField;
    @FXML private TextField compostKgField;
    @FXML private TextField publicTransportTripsField;
    @FXML private TextField flightsPerYearField;

    @FXML private Label currentScoreLabel;
    @FXML private Label scenarioScoreLabel;
    @FXML private Label scoreDifferenceLabel;

    @FXML private TextField scenarioNameField;
    @FXML private VBox savedScenariosList;

    @FXML private Label errorLabel;
    @FXML private Label statusLabel;

    public ScenariosController(Navigator nav, AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        loadCurrentHouseholdData();

        if (scenario != null) {
            refreshSavedScenarios();
        }
    }

    private void loadCurrentHouseholdData() {
        boolean hasHousehold = ctx.session.hasActiveHousehold();

        noHouseholdSection.setVisible(!hasHousehold);
        noHouseholdSection.setManaged(!hasHousehold);
        scenarioSection.setVisible(hasHousehold);
        scenarioSection.setManaged(hasHousehold);

        if (!hasHousehold) {
            return;
        }

        Household household = ctx.session.getCurrentHousehold();

        List<EnergyEntry> energyEntries =
                ctx.energyService.findEntriesForHousehold(household);
        List<WaterEntry> waterEntries =
                ctx.waterService.findEntriesForHousehold(household);
        List<WasteEntry> wasteEntries =
                ctx.wasteService.findEntriesForHousehold(household);
        List<TransportEntry> transportEntries =
                ctx.transportService.findEntriesForHousehold(household);

        if (energyEntries.isEmpty()
                || waterEntries.isEmpty()
                || wasteEntries.isEmpty()
                || transportEntries.isEmpty()) {

            scenarioSection.setVisible(false);
            scenarioSection.setManaged(false);

            showError(
                    "Record energy, water, waste and transport information "
                            + "before creating a scenario."
            );
            return;
        }

        EnergyEntry energy = latestEnergy(energyEntries);
        WaterEntry water = latestWater(waterEntries);
        WasteEntry waste = latestWaste(wasteEntries);
        TransportEntry transport = latestTransport(transportEntries);

        currentValues = ctx.scenarioService.createScenario(
                energy,
                water,
                waste,
                transport
        );
        scenario = currentValues.copy();

        populateFields();
        showComparison(compareToCurrent());
    }

    private EnergyEntry latestEnergy(List<EnergyEntry> entries) {
        return entries.stream()
                .max(Comparator
                        .comparing(EnergyEntry::getPeriod)
                        .thenComparingLong(EnergyEntry::getId))
                .orElseThrow();
    }

    private WaterEntry latestWater(List<WaterEntry> entries) {
        return entries.stream()
                .max(Comparator
                        .comparing(WaterEntry::getPeriod)
                        .thenComparingLong(WaterEntry::getId))
                .orElseThrow();
    }

    private WasteEntry latestWaste(List<WasteEntry> entries) {
        return entries.stream()
                .max(Comparator
                        .comparing(WasteEntry::getPeriod)
                        .thenComparingLong(WasteEntry::getId))
                .orElseThrow();
    }

    private TransportEntry latestTransport(List<TransportEntry> entries) {
        return entries.stream()
                .max(Comparator
                        .comparing(TransportEntry::getPeriod)
                        .thenComparingLong(TransportEntry::getId))
                .orElseThrow();
    }

    private void populateFields() {
        energyKwhField.setText(String.valueOf(scenario.getEnergyKwh()));

        if (scenario.getSolarGenerationKwh() == null) {
            solarGenerationKwhField.clear();
        } else {
            solarGenerationKwhField.setText(
                    String.valueOf(scenario.getSolarGenerationKwh())
            );
        }

        waterLitresField.setText(String.valueOf(scenario.getWaterLitres()));
        generalWasteKgField.setText(String.valueOf(scenario.getGeneralWasteKg()));
        recycledWasteKgField.setText(String.valueOf(scenario.getRecycledWasteKg()));
        compostKgField.setText(String.valueOf(scenario.getCompostKg()));

        publicTransportTripsField.setText(
                String.valueOf(scenario.getPublicTransportTripsPerWeek())
        );

        flightsPerYearField.setText(
                String.valueOf(scenario.getFlightsPerYear())
        );
    }

    @FXML
    private void handleApplyScenarioChanges() {
        if (!applyFieldsToScenario()) {
            return;
        }

        showComparison(compareToCurrent());
        showStatus("Scenario updated. Your real household data has not been changed.");
    }

    /** Copies the form into the in-memory scenario. Returns false (with an error shown) if it is invalid. */
    private boolean applyFieldsToScenario() {
        try {
            double energyKwh =
                    Double.parseDouble(energyKwhField.getText().trim());

            Double solarGenerationKwh =
                    solarGenerationKwhField.getText().isBlank()
                            ? null
                            : Double.parseDouble(
                                    solarGenerationKwhField.getText().trim()
                            );

            double waterLitres =
                    Double.parseDouble(waterLitresField.getText().trim());

            double generalWasteKg =
                    Double.parseDouble(generalWasteKgField.getText().trim());

            double recycledWasteKg =
                    Double.parseDouble(recycledWasteKgField.getText().trim());

            double compostKg =
                    Double.parseDouble(compostKgField.getText().trim());

            double publicTransportTrips =
                    Double.parseDouble(
                            publicTransportTripsField.getText().trim()
                    );

            double flightsPerYear =
                    Double.parseDouble(flightsPerYearField.getText().trim());

            if (energyKwh < 0
                    || (solarGenerationKwh != null && solarGenerationKwh < 0)
                    || waterLitres < 0
                    || generalWasteKg < 0
                    || recycledWasteKg < 0
                    || compostKg < 0
                    || publicTransportTrips < 0
                    || flightsPerYear < 0) {

                showError("Scenario values cannot be negative.");
                return false;
            }

            scenario.setEnergyKwh(energyKwh);
            scenario.setSolarGenerationKwh(solarGenerationKwh);
            scenario.setWaterLitres(waterLitres);
            scenario.setGeneralWasteKg(generalWasteKg);
            scenario.setRecycledWasteKg(recycledWasteKg);
            scenario.setCompostKg(compostKg);
            scenario.setPublicTransportTripsPerWeek(publicTransportTrips);
            scenario.setFlightsPerYear(flightsPerYear);

            hideError();
            return true;

        } catch (NumberFormatException e) {
            showError("Enter valid numbers for all scenario values.");
            return false;
        }
    }

    @FXML
    private void handleResetScenario() {
        hideError();

        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        loadCurrentHouseholdData();
    }

    // US-28: compare scenario score to current score
    private ScenarioComparison compareToCurrent() {
        return ctx.scenarioService.compareToCurrent(
                currentValues,
                scenario,
                ctx.session.getCurrentHousehold()
        );
    }

    private void showComparison(ScenarioComparison comparison) {
        currentScoreLabel.setText(formatScore(comparison.getCurrentScore()));
        scenarioScoreLabel.setText(formatScore(comparison.getScenarioScore()));
        scoreDifferenceLabel.setText(describeDifference(comparison));

        scoreDifferenceLabel.getStyleClass().removeAll("success-text", "error-text", "muted-text");
        scoreDifferenceLabel.getStyleClass().add(switch (comparison.getDirection()) {
            case IMPROVED -> "success-text";
            case WORSENED -> "error-text";
            case UNCHANGED -> "muted-text";
        });
    }

    private String describeDifference(ScenarioComparison comparison) {
        String points = formatScore(Math.abs(comparison.getDifference())) + " points";

        return switch (comparison.getDirection()) {
            case IMPROVED -> "+" + points + " - better than your current score";
            case WORSENED -> "-" + points + " - worse than your current score";
            case UNCHANGED -> "No change from your current score";
        };
    }

    private String formatScore(double score) {
        return String.format("%.1f", score);
    }

    // US-29: save a scenario
    @FXML
    private void handleSaveScenario() {
        if (!ctx.session.canSaveScenario()) {
            showError("Sign in to save a scenario.");
            return;
        }

        if (!applyFieldsToScenario()) {
            return;
        }

        try {
            SavedScenario saved = ctx.scenarioService.saveScenario(
                    ctx.session.getCurrentUser(),
                    ctx.session.getCurrentHousehold(),
                    scenarioNameField.getText(),
                    currentValues,
                    scenario
            );

            scenarioNameField.clear();
            showComparison(compareToCurrent());
            showStatus("Saved \"" + saved.getName() + "\" for your household.");
            refreshSavedScenarios();

        } catch (IllegalArgumentException e) {
            showError("Enter a name for the scenario before saving.");
        }
    }

    // US-30: view saved scenarios
    private void refreshSavedScenarios() {
        savedScenariosList.getChildren().clear();

        List<SavedScenario> savedScenarios =
                ctx.scenarioService.findSavedScenarios(ctx.session.getCurrentHousehold());

        if (savedScenarios.isEmpty()) {
            Label emptyLabel = new Label("No scenarios have been saved for this household yet.");
            emptyLabel.getStyleClass().add("muted-text");
            savedScenariosList.getChildren().add(emptyLabel);
            return;
        }

        for (SavedScenario saved : savedScenarios) {
            savedScenariosList.getChildren().add(savedScenarioRow(saved));
        }
    }

    private HBox savedScenarioRow(SavedScenario saved) {
        ScenarioComparison comparison =
                new ScenarioComparison(saved.getBaselineScore(), saved.getProjectedScore());

        Label nameLabel = new Label(saved.getName());
        nameLabel.getStyleClass().add("value-text");

        Label scoresLabel = new Label(
                "Score " + formatScore(comparison.getScenarioScore())
                        + " (current was " + formatScore(comparison.getCurrentScore()) + ")"
        );
        scoresLabel.getStyleClass().add("muted-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button openButton = new Button("Open");
        openButton.getStyleClass().add("btn-secondary");
        openButton.setOnAction(event -> handleOpenSavedScenario(saved));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("btn-danger");
        deleteButton.setOnAction(event -> handleDeleteSavedScenario(saved));

        HBox row = new HBox(16, nameLabel, scoresLabel, spacer, openButton, deleteButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("history-row");
        return row;
    }

    private void handleOpenSavedScenario(SavedScenario saved) {
        scenario = saved.getValues().copy();

        hideError();
        populateFields();
        showComparison(new ScenarioComparison(saved.getBaselineScore(), saved.getProjectedScore()));
        showStatus(
                "Viewing saved scenario \"" + saved.getName() + "\" with the scores it had when it was saved. "
                        + "Apply Scenario Changes compares it with your current data."
        );
    }

    // US-31: delete a saved scenario
    private void handleDeleteSavedScenario(SavedScenario saved) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Delete Scenario");
        confirmation.setHeaderText("Delete the scenario \"" + saved.getName() + "\"?");
        confirmation.setContentText(
                "It will be removed for every member of your household. "
                        + "Your household data will not be changed."
        );

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ctx.scenarioService.deleteScenario(
                        ctx.session.getCurrentHousehold(),
                        saved.getId()
                );

                showStatus("Deleted \"" + saved.getName() + "\".");
                refreshSavedScenarios();
            }
        });
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
