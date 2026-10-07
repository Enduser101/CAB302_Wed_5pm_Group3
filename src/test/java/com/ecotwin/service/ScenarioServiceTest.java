package com.ecotwin.service;

import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScenarioServiceTest {

    @Test
    void createsScenarioFromCurrentHouseholdData() {
        EnergyEntry energy = new EnergyEntry(
                1, 1, "2026-10", 500, 100.0,
                null, 1L, null
        );

        WaterEntry water = new WaterEntry(
                1, 1, "2026-10", 4000,
                null, 1L, null
        );

        WasteEntry waste = new WasteEntry(
                1, 1, "2026-10", 30, 15, 5,
                1L, null
        );

        TransportEntry transport = new TransportEntry(
                1, 1, "2026-10", 8, 2,
                1L, null
        );

        ScenarioService service = new ScenarioService();

        SustainabilityScenario scenario =
                service.createScenario(energy, water, waste, transport);

        assertEquals(500, scenario.getEnergyKwh());
        assertEquals(100, scenario.getSolarGenerationKwh());
        assertEquals(4000, scenario.getWaterLitres());

        assertEquals(30, scenario.getGeneralWasteKg());
        assertEquals(15, scenario.getRecycledWasteKg());
        assertEquals(5, scenario.getCompostKg());

        assertEquals(8, scenario.getPublicTransportTripsPerWeek());
        assertEquals(2, scenario.getFlightsPerYear());
    }

    @Test
    void changingScenarioDoesNotChangeCurrentEntries() {
        EnergyEntry energy = new EnergyEntry(
                1, 1, "2026-10", 500, 100.0,
                null, 1L, null
        );

        WaterEntry water = new WaterEntry(
                1, 1, "2026-10", 4000,
                null, 1L, null
        );

        WasteEntry waste = new WasteEntry(
                1, 1, "2026-10", 30, 15, 5,
                1L, null
        );

        TransportEntry transport = new TransportEntry(
                1, 1, "2026-10", 8, 2,
                1L, null
        );

        ScenarioService service = new ScenarioService();

        SustainabilityScenario scenario =
                service.createScenario(energy, water, waste, transport);

        scenario.setEnergyKwh(350);
        scenario.setWaterLitres(3000);
        scenario.setGeneralWasteKg(20);
        scenario.setPublicTransportTripsPerWeek(12);

        assertEquals(500, energy.getElectricityKwh());
        assertEquals(4000, water.getLitres());
        assertEquals(30, waste.getGeneralKg());
        assertEquals(8, transport.getPublicTransportTripsPerWeek());

        assertEquals(350, scenario.getEnergyKwh());
        assertEquals(3000, scenario.getWaterLitres());
        assertEquals(20, scenario.getGeneralWasteKg());
        assertEquals(12, scenario.getPublicTransportTripsPerWeek());
    }
}
