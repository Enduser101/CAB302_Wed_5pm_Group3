package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;
import com.ecotwin.service.ScenarioService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

/**
 * US-27: Create a temporary sustainability scenario.
 *
 * Scenario values are copied from current household data and can be changed
 * without changing the household's real resource records.
 */
public class ScenariosController {

    private final AppContext ctx;
    private final ScenarioService scenarioService = new ScenarioService();

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

    @FXML private Label errorLabel;
    @FXML private Label statusLabel;

    public ScenariosController(Navigator nav, AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        loadCurrentHouseholdData();
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

        scenario = scenarioService.createScenario(
                energy,
                water,
                waste,
                transport
        );

        populateFields();
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
                return;
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

            statusLabel.setText(
                    "Scenario updated. Your real household data has not been changed."
            );
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);

        } catch (NumberFormatException e) {
            showError("Enter valid numbers for all scenario values.");
        }
    }

    @FXML
    private void handleResetScenario() {
        hideError();

        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        loadCurrentHouseholdData();
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
