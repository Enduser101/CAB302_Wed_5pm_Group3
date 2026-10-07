package com.ecotwin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SustainabilityScenarioTest {

    @Test
    void scenarioStoresHouseholdValues() {
        SustainabilityScenario scenario =
                new SustainabilityScenario(
                        500,
                        100.0,
                        4000,
                        30,
                        15,
                        5,
                        8,
                        2
                );

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
    void scenarioValuesCanChangeIndependently() {
        SustainabilityScenario scenario =
                new SustainabilityScenario(
                        500,
                        100.0,
                        4000,
                        30,
                        15,
                        5,
                        8,
                        2
                );

        scenario.setEnergyKwh(350);
        scenario.setWaterLitres(3000);
        scenario.setGeneralWasteKg(20);
        scenario.setPublicTransportTripsPerWeek(12);

        assertEquals(350, scenario.getEnergyKwh());
        assertEquals(3000, scenario.getWaterLitres());
        assertEquals(20, scenario.getGeneralWasteKg());
        assertEquals(12, scenario.getPublicTransportTripsPerWeek());
    }
}
