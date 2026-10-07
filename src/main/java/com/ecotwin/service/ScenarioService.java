package com.ecotwin.service;

import com.ecotwin.dao.ScenarioDao;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.SavedScenario;
import com.ecotwin.model.ScenarioComparison;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.model.TransportEntry;
import com.ecotwin.model.User;
import com.ecotwin.model.WasteEntry;
import com.ecotwin.model.WaterEntry;

import java.util.List;

/**
 * Creates temporary sustainability scenarios from current household data.
 *
 * Scenario changes remain in memory and do not update the household's
 * persisted resource records. Saving a scenario (US-29) stores it in its own
 * table, again without touching the household's resource records or history.
 */
public class ScenarioService {

    private final ScenarioDao scenarioDao;
    private final ScoreCalculator scoreCalculator;

    public ScenarioService(ScenarioDao scenarioDao, ScoreCalculator scoreCalculator) {
        this.scenarioDao = scenarioDao;
        this.scoreCalculator = scoreCalculator;
    }

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

    /** US-28: score the scenario and the current household data with the same calculation. */
    public ScenarioComparison compareToCurrent(SustainabilityScenario current,
                                               SustainabilityScenario scenario,
                                               Household household) {
        double currentScore = scoreCalculator.calculate(current, household.getOccupants());
        double scenarioScore = scoreCalculator.calculate(scenario, household.getOccupants());
        return new ScenarioComparison(currentScore, scenarioScore);
    }

    /** US-29: save the scenario under a name, for every member of the household to see. */
    public SavedScenario saveScenario(User actor, Household household, String name,
                                      SustainabilityScenario current,
                                      SustainabilityScenario scenario) {
        if (actor == null) {
            throw new IllegalStateException("Only registered users can save a scenario");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Scenario name cannot be empty");
        }

        ScenarioComparison comparison = compareToCurrent(current, scenario, household);
        return scenarioDao.create(
                household.getId(),
                actor.getId(),
                name.trim(),
                scenario,
                comparison.getCurrentScore(),
                comparison.getScenarioScore()
        );
    }

    /** US-30: every scenario saved by any member of the household. */
    public List<SavedScenario> findSavedScenarios(Household household) {
        return scenarioDao.findByHousehold(household.getId());
    }

    /** US-31: permanently remove a saved scenario. Household resource data is not touched. */
    public void deleteScenario(Household household, long scenarioId) {
        scenarioDao.delete(scenarioId, household.getId());
    }
}
