package com.ecotwin.service;

import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;

/**
 * Creates temporary sustainability scenarios from current household data.
 *
 * Scenario changes remain in memory and do not update the household's
 * persisted resource records.
 */
public class ScenarioService {

    public SustainabilityScenario createScenario(EnergyEntry energy,
                                                 WaterEntry water,
                                                 WasteEntry waste,
                                                 TransportEntry transport) {

        return new SustainabilityScenario(
                energy.getElectricityKwh(),
                energy.getSolarGenerationKwh(),
                water.getLitres(),
                waste.getGeneralKg(),
                waste.getRecycledKg(),
                waste.getCompostKg(),
                transport.getPublicTransportTripsPerWeek(),
                transport.getFlightsPerYear()
        );
    }
}
