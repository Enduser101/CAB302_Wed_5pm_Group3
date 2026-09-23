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
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.List;

/** US-15..18: record energy, water, waste and transport information for the current household. */
public class ResourcesController {
    private Long editingEnergyEntryId;
    private Long editingWaterEntryId;
    private Long editingWasteEntryId;
    private Long editingTransportEntryId;
    private Long editingVehicleId;

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
        noHouseholdSection.setVisible(!hasHousehold);
        noHouseholdSection.setManaged(!hasHousehold);
        resourcesTabPane.setVisible(hasHousehold);
        resourcesTabPane.setManaged(hasHousehold);

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
        try {
            double electricityKwh = Double.parseDouble(electricityKwhField.getText().trim());
            Double solarGenerationKwh = solarGenerationKwhField.getText().isBlank()
                ? null : Double.parseDouble(solarGenerationKwhField.getText().trim());
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            if (editingEnergyEntryId == null) {
                ctx.energyService.recordEntry(
                        user, household, electricityKwh,
                        solarGenerationKwh, energyNotesField.getText()
                );
            } else {
                ctx.energyService.updateEntry(
                        user, household, editingEnergyEntryId,
                        electricityKwh, solarGenerationKwh, energyNotesField.getText()
                );
                editingEnergyEntryId = null;
            }
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
            String solar = entry.getSolarGenerationKwh() != null
                    ? ", " + entry.getSolarGenerationKwh() + " kWh solar"
                    : "";

            Label entryLabel = mutedLabel(
                    entry.getPeriod() + " — " + entry.getElectricityKwh() + " kWh" + solar
            );
            Button editButton = new Button("✎\u00A0\u00A0Edit");
            editButton.getStyleClass().add("edit-button");

            editButton.setOnAction(event-> {
                editingEnergyEntryId = entry.getId();

                electricityKwhField.setText(String.valueOf(entry.getElectricityKwh()));

                if (entry.getSolarGenerationKwh() != null) {
                    solarGenerationKwhField.setText(
                            String.valueOf(entry.getSolarGenerationKwh())
                    );
                } else {
                    solarGenerationKwhField.clear();
                }

                energyNotesField.setText(
                        entry.getNotes() != null ? entry.getNotes() : ""
                );
            });

            HBox row = new HBox(10);
            row.getStyleClass().add("history-row");
            row.getChildren().addAll(entryLabel, editButton);

            entryLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(entryLabel, javafx.scene.layout.Priority.ALWAYS);

            energyEntriesList.getChildren().add(row);
        }
    }
    // US-16: record water information

    @FXML
    private void handleSaveWater() {
        try {
            double litres = Double.parseDouble(litresField.getText().trim());
            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            if (editingWaterEntryId == null) {
                ctx.waterService.recordEntry(
                        user, household, litres, waterNotesField.getText()
                );
            } else {
                ctx.waterService.updateEntry(
                        user, household, editingWaterEntryId,
                        litres, waterNotesField.getText()
                );
                editingWaterEntryId = null;
            }

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
            waterEntriesList.getChildren().add(
                    mutedLabel("No water readings yet")
            );
            return;
        }

        for (WaterEntry entry : entries) {

            Label entryLabel = mutedLabel(
                    entry.getPeriod() + " — " + entry.getLitres() + " L"
            );

            Button editButton = new Button("✎\u00A0\u00A0Edit");
            editButton.getStyleClass().add("edit-button");

            editButton.setOnAction(event -> {
                editingWaterEntryId = entry.getId();

                litresField.setText(
                        String.valueOf(entry.getLitres())
                );

                waterNotesField.setText(
                        entry.getNotes() != null ? entry.getNotes() : ""
                );
            });

            HBox row = new HBox(10);
            row.getStyleClass().add("history-row");

            entryLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(
                    entryLabel,
                    javafx.scene.layout.Priority.ALWAYS
            );

            row.getChildren().addAll(entryLabel, editButton);
            waterEntriesList.getChildren().add(row);
        }
    }


    // US-17: record waste information

    @FXML
    private void handleSaveWaste() {
        try {
            double generalKg = Double.parseDouble(generalKgField.getText().trim());
            double recycledKg = Double.parseDouble(recycledKgField.getText().trim());
            double compostKg = Double.parseDouble(compostKgField.getText().trim());

            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            if (editingWasteEntryId == null) {
                ctx.wasteService.recordEntry(
                        user,
                        household,
                        generalKg,
                        recycledKg,
                        compostKg
                );
            } else {
                ctx.wasteService.updateEntry(
                        user,
                        household,
                        editingWasteEntryId,
                        generalKg,
                        recycledKg,
                        compostKg
                );

                editingWasteEntryId = null;
            }

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
        List<WasteEntry> entries =
                ctx.wasteService.findEntriesForHousehold(household);

        wasteEntriesList.getChildren().clear();

        if (entries.isEmpty()) {
            wasteEntriesList.getChildren().add(
                    mutedLabel("No waste readings yet")
            );
            return;
        }

        for (WasteEntry entry : entries) {

            Label entryLabel = mutedLabel(
                    entry.getPeriod()
                            + " — General: " + entry.getGeneralKg() + " kg"
                            + " | Recycled: " + entry.getRecycledKg() + " kg"
                            + " | Compost: " + entry.getCompostKg() + " kg"
            );

            Button editButton = new Button("✎\u00A0\u00A0Edit");
            editButton.getStyleClass().add("edit-button");

            editButton.setOnAction(event -> {
                editingWasteEntryId = entry.getId();

                generalKgField.setText(
                        String.valueOf(entry.getGeneralKg())
                );

                recycledKgField.setText(
                        String.valueOf(entry.getRecycledKg())
                );

                compostKgField.setText(
                        String.valueOf(entry.getCompostKg())
                );
            });

            HBox row = new HBox(10);
            row.getStyleClass().add("history-row");

            entryLabel.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(
                    entryLabel,
                    javafx.scene.layout.Priority.ALWAYS
            );

            row.getChildren().addAll(entryLabel, editButton);

            wasteEntriesList.getChildren().add(row);
        }
    }



    // US-18: record transport information

    @FXML
    private void handleAddVehicle() {
        try {
            String label = vehicleLabelField.getText();
            String fuelType = vehicleFuelTypeCombo.getValue();
            double kmPerWeek =
                    Double.parseDouble(vehicleKmPerWeekField.getText().trim());

            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            if (editingVehicleId == null) {
                ctx.transportService.addVehicle(
                        user,
                        household,
                        label,
                        fuelType,
                        kmPerWeek
                );
            } else {
                ctx.transportService.updateVehicle(
                        user,
                        household,
                        editingVehicleId,
                        label,
                        fuelType,
                        kmPerWeek
                );

                editingVehicleId = null;
            }

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

        var vehicles =
                ctx.transportService.findVehiclesForHousehold(household);

        if (vehicles.isEmpty()) {
            vehiclesList.getChildren().add(
                    mutedLabel("No vehicles added yet")
            );
            return;
        }

        for (var vehicle : vehicles) {

            Label vehicleLabel = mutedLabel(
                    vehicle.getLabel()
                            + " (" + vehicle.getFuelType() + ") — "
                            + vehicle.getKmPerWeek()
                            + " km/week"
            );

            Button editButton = new Button("✎\u00A0\u00A0Edit");
            editButton.getStyleClass().add("edit-button");

            editButton.setOnAction(event -> {
                editingVehicleId = vehicle.getId();

                vehicleLabelField.setText(
                        vehicle.getLabel()
                );

                vehicleFuelTypeCombo.setValue(
                        vehicle.getFuelType()
                );

                vehicleKmPerWeekField.setText(
                        String.valueOf(vehicle.getKmPerWeek())
                );
            });

            HBox row = new HBox(10);
            row.getStyleClass().add("history-row");

            vehicleLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(
                    vehicleLabel,
                    javafx.scene.layout.Priority.ALWAYS
            );

            row.getChildren().addAll(vehicleLabel, editButton);
            vehiclesList.getChildren().add(row);
        }
    }
    @FXML
    private void handleSaveTransport() {
        try {
            double publicTransportTripsPerWeek =
                    Double.parseDouble(publicTransportTripsField.getText().trim());

            double flightsPerYear =
                    Double.parseDouble(flightsPerYearField.getText().trim());

            User user = ctx.session.getCurrentUser();
            Household household = ctx.session.getCurrentHousehold();

            if (editingTransportEntryId == null) {
                ctx.transportService.recordEntry(
                        user,
                        household,
                        publicTransportTripsPerWeek,
                        flightsPerYear
                );
            } else {
                ctx.transportService.updateEntry(
                        user,
                        household,
                        editingTransportEntryId,
                        publicTransportTripsPerWeek,
                        flightsPerYear
                );

                editingTransportEntryId = null;
            }

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
        List<TransportEntry> entries =
                ctx.transportService.findEntriesForHousehold(household);

        transportEntriesList.getChildren().clear();

        if (entries.isEmpty()) {
            transportEntriesList.getChildren().add(
                    mutedLabel("No transport information recorded yet")
            );
            return;
        }

        for (TransportEntry entry : entries) {

            Label entryLabel = mutedLabel(
                    entry.getPeriod()
                            + " — "
                            + entry.getPublicTransportTripsPerWeek()
                            + " PT trips/week, "
                            + entry.getFlightsPerYear()
                            + " flights/year"
            );

            Button editButton = new Button("✎\u00A0\u00A0Edit");
            editButton.getStyleClass().add("edit-button");

            editButton.setOnAction(event -> {
                editingTransportEntryId = entry.getId();

                publicTransportTripsField.setText(
                        String.valueOf(entry.getPublicTransportTripsPerWeek())
                );

                flightsPerYearField.setText(
                        String.valueOf(entry.getFlightsPerYear())
                );
            });

            HBox row = new HBox(10);
            row.getStyleClass().add("history-row");

            entryLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(
                    entryLabel,
                    javafx.scene.layout.Priority.ALWAYS
            );

            row.getChildren().addAll(entryLabel, editButton);
            transportEntriesList.getChildren().add(row);
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

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
