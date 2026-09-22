package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.User;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

/** US-15..18: record energy, water, waste and transport information for the current household. */
public class ResourcesController {

    private final Navigator nav;
    private final AppContext ctx;

    @FXML private VBox noHouseholdSection;
    @FXML private TabPane resourcesTabPane;

    @FXML private TextField electricityKwhField;
    @FXML private TextField solarGenerationKwhField;
    @FXML private TextField energyNotesField;
    @FXML private Label energyErrorLabel;
    @FXML private VBox energyEntriesList;

    @FXML private TextField litresField;
    @FXML private TextField waterNotesField;
    @FXML private Label waterErrorLabel;
    @FXML private VBox waterEntriesList;

    @FXML private TextField generalKgField;
    @FXML private TextField recycledKgField;
    @FXML private TextField compostKgField;
    @FXML private Label wasteErrorLabel;
    @FXML private VBox wasteEntriesList;

    @FXML private TextField vehicleLabelField;
    @FXML private ComboBox<String> vehicleFuelTypeCombo;
    @FXML private TextField vehicleKmPerWeekField;
    @FXML private Label vehicleErrorLabel;
    @FXML private VBox vehiclesList;

    @FXML private TextField publicTransportTripsField;
    @FXML private TextField flightsPerYearField;
    @FXML private Label transportErrorLabel;
    @FXML private VBox transportEntriesList;

    public ResourcesController(Navigator nav, AppContext ctx) {
        this.nav = nav;
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        vehicleFuelTypeCombo.getItems().addAll("Petrol", "Diesel", "Hybrid", "EV");
        refresh();
    }

    private void refresh() {
        boolean hasHousehold = ctx.session.hasActiveHousehold();
        boolean showTabs = hasHousehold || ctx.session.isGuest(); //let guests type and look but they can't save.
        noHouseholdSection.setVisible(!showTabs);
        noHouseholdSection.setManaged(!showTabs);
        resourcesTabPane.setVisible(showTabs);
        resourcesTabPane.setManaged(showTabs);

        if (!hasHousehold) {
            return;
        }

        Household household = ctx.session.getCurrentHousehold();
        refreshEnergyList(household);
        refreshWaterList(household);
        refreshWasteList(household);
        refreshVehiclesList(household);
        refreshTransportList(household);
    }

    // US-15: record energy information

    @FXML
    private void handleSaveEnergy() {
        if (blockedFromSaving(energyErrorLabel)) return;
        try {
            double electricityKwh = Double.parseDouble(electricityKwhField.getText().trim());
            Double solarGenerationKwh = solarGenerationKwhField.getText().isBlank()
                ? null : Double.parseDouble(solarGenerationKwhField.getText().trim());
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            ctx.energyService.recordEntry(user, household, electricityKwh, solarGenerationKwh, energyNotesField.getText());
            hideError(energyErrorLabel);
            electricityKwhField.clear();
            solarGenerationKwhField.clear();
            energyNotesField.clear();
            refreshEnergyList(household);
        } catch (NumberFormatException e) {
            showError(energyErrorLabel, "Enter a valid number");
        } catch (IllegalArgumentException e) {
            showError(energyErrorLabel, e.getMessage());
        }
    }

    private void refreshEnergyList(Household household) {
        List<EnergyEntry> entries = ctx.energyService.findEntriesForHousehold(household);
        energyEntriesList.getChildren().clear();
        if (entries.isEmpty()) {
            energyEntriesList.getChildren().add(mutedLabel("No energy readings yet"));
            return;
        }
        for (EnergyEntry entry : entries) {
            String solar = entry.getSolarGenerationKwh() != null ? ", " + entry.getSolarGenerationKwh() + " kWh solar" : "";
            energyEntriesList.getChildren().add(
                mutedLabel(entry.getPeriod() + " — " + entry.getElectricityKwh() + " kWh" + solar));
        }
    }

    // US-16: record water information

    @FXML
    private void handleSaveWater() {
        if (blockedFromSaving(waterErrorLabel)) return;
        try {
            double litres = Double.parseDouble(litresField.getText().trim());
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            ctx.waterService.recordEntry(user, household, litres, waterNotesField.getText());
            hideError(waterErrorLabel);
            litresField.clear();
            waterNotesField.clear();
            refreshWaterList(household);
        } catch (NumberFormatException e) {
            showError(waterErrorLabel, "Enter a valid number");
        } catch (IllegalArgumentException e) {
            showError(waterErrorLabel, e.getMessage());
        }
    }

    private void refreshWaterList(Household household) {
        List<WaterEntry> entries = ctx.waterService.findEntriesForHousehold(household);
        waterEntriesList.getChildren().clear();
        if (entries.isEmpty()) {
            waterEntriesList.getChildren().add(mutedLabel("No water readings yet"));
            return;
        }
        for (WaterEntry entry : entries) {
            waterEntriesList.getChildren().add(mutedLabel(entry.getPeriod() + " — " + entry.getLitres() + " L"));
        }
    }

    // US-17: record waste information

    @FXML
    private void handleSaveWaste() {
        if (blockedFromSaving(wasteErrorLabel)) return;
        try {
            double generalKg = Double.parseDouble(generalKgField.getText().trim());
            double recycledKg = Double.parseDouble(recycledKgField.getText().trim());
            double compostKg = Double.parseDouble(compostKgField.getText().trim());
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            ctx.wasteService.recordEntry(user, household, generalKg, recycledKg, compostKg);
            hideError(wasteErrorLabel);
            generalKgField.clear();
            recycledKgField.clear();
            compostKgField.clear();
            refreshWasteList(household);
        } catch (NumberFormatException e) {
            showError(wasteErrorLabel, "Enter valid numbers");
        } catch (IllegalArgumentException e) {
            showError(wasteErrorLabel, e.getMessage());
        }
    }

    private void refreshWasteList(Household household) {
        List<WasteEntry> entries = ctx.wasteService.findEntriesForHousehold(household);
        wasteEntriesList.getChildren().clear();
        if (entries.isEmpty()) {
            wasteEntriesList.getChildren().add(mutedLabel("No waste readings yet"));
            return;
        }
        for (WasteEntry entry : entries) {
            wasteEntriesList.getChildren().add(mutedLabel(entry.getPeriod() + " — general " + entry.getGeneralKg()
                + "kg, recycled " + entry.getRecycledKg() + "kg, compost " + entry.getCompostKg() + "kg"));
        }
    }

    // US-18: record transport information

    @FXML
    private void handleAddVehicle() {
        if (blockedFromSaving(vehicleErrorLabel)) return;
        try {
            String label = vehicleLabelField.getText();
            String fuelType = vehicleFuelTypeCombo.getValue();
            double kmPerWeek = Double.parseDouble(vehicleKmPerWeekField.getText().trim());
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            ctx.transportService.addVehicle(user, household, label, fuelType, kmPerWeek);
            hideError(vehicleErrorLabel);
            vehicleLabelField.clear();
            vehicleFuelTypeCombo.setValue(null);
            vehicleKmPerWeekField.clear();
            refreshVehiclesList(household);
        } catch (NumberFormatException e) {
            showError(vehicleErrorLabel, "Enter a valid number");
        } catch (IllegalArgumentException e) {
            showError(vehicleErrorLabel, e.getMessage());
        }
    }

    private void refreshVehiclesList(Household household) {
        vehiclesList.getChildren().clear();
        var vehicles = ctx.transportService.findVehiclesForHousehold(household);
        if (vehicles.isEmpty()) {
            vehiclesList.getChildren().add(mutedLabel("No vehicles added yet"));
            return;
        }
        vehicles.forEach(vehicle -> vehiclesList.getChildren().add(
            mutedLabel(vehicle.getLabel() + " (" + vehicle.getFuelType() + ") — " + vehicle.getKmPerWeek() + " km/week")));
    }

    @FXML
    private void handleSaveTransport() {
        if (blockedFromSaving(transportErrorLabel)) return;
        try {
            double publicTransportTripsPerWeek = Double.parseDouble(publicTransportTripsField.getText().trim());
            double flightsPerYear = Double.parseDouble(flightsPerYearField.getText().trim());
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            ctx.transportService.recordEntry(user, household, publicTransportTripsPerWeek, flightsPerYear);
            hideError(transportErrorLabel);
            publicTransportTripsField.clear();
            flightsPerYearField.clear();
            refreshTransportList(household);
        } catch (NumberFormatException e) {
            showError(transportErrorLabel, "Enter valid numbers");
        } catch (IllegalArgumentException e) {
            showError(transportErrorLabel, e.getMessage());
        }
    }

    private void refreshTransportList(Household household) {
        List<TransportEntry> entries = ctx.transportService.findEntriesForHousehold(household);
        transportEntriesList.getChildren().clear();
        if (entries.isEmpty()) {
            transportEntriesList.getChildren().add(mutedLabel("No transport information recorded yet"));
            return;
        }
        for (TransportEntry entry : entries) {
            transportEntriesList.getChildren().add(mutedLabel(entry.getPeriod() + " — "
                + entry.getPublicTransportTripsPerWeek() + " PT trips/week, " + entry.getFlightsPerYear() + " flights/year"));
        }
    }

    private Label mutedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    /** US-03 / #85: guests and users without a household see a message instead of saving. */
    private boolean blockedFromSaving(Label errorLabel) {
        if (ctx.session.canRecordEntries()) return false;
        showError(errorLabel, "Sign in and join a household to save readings");
        return true;
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
