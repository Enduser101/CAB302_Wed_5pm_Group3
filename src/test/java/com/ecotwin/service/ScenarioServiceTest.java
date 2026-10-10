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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ScenarioServiceTest {

    private final ScenarioDao scenarioDao = mock(ScenarioDao.class);
    private final ScoreCalculator scoreCalculator = mock(ScoreCalculator.class);
    private final ScenarioService service = new ScenarioService(scenarioDao, scoreCalculator);

    private final User user = new User(1, "resident", "r@example.com", "hash", "Resident", "now");
    private final Household household = new Household(7, "Test House", "ABC234", 2, "House", "QLD", "now");

    private final SustainabilityScenario current =
            new SustainabilityScenario(500, 100.0, 4000, 30, 15, 5, 8, 2);
    private final SustainabilityScenario scenario =
            new SustainabilityScenario(350, 100.0, 3000, 20, 15, 5, 12, 2);

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

    // US-28: compare scenario score to current score

    @Test
    void comparisonShowsBothScoresAndTheDifference() {
        when(scoreCalculator.calculate(current, 2)).thenReturn(50.0);
        when(scoreCalculator.calculate(scenario, 2)).thenReturn(62.5);

        ScenarioComparison comparison = service.compareToCurrent(current, scenario, household);

        assertEquals(50.0, comparison.getCurrentScore());
        assertEquals(62.5, comparison.getScenarioScore());
        assertEquals(12.5, comparison.getDifference());
    }

    @Test
    void scenarioScoreUsesTheSameCalculationAsTheCurrentScore() {
        service.compareToCurrent(current, scenario, household);

        verify(scoreCalculator, times(1)).calculate(current, household.getOccupants());
        verify(scoreCalculator, times(1)).calculate(scenario, household.getOccupants());
    }

    @Test
    void higherScenarioScoreIsReportedAsAnImprovement() {
        when(scoreCalculator.calculate(current, 2)).thenReturn(50.0);
        when(scoreCalculator.calculate(scenario, 2)).thenReturn(62.5);

        ScenarioComparison comparison = service.compareToCurrent(current, scenario, household);

        assertEquals(ScenarioComparison.Direction.IMPROVED, comparison.getDirection());
    }

    @Test
    void lowerScenarioScoreIsReportedAsWorse() {
        when(scoreCalculator.calculate(current, 2)).thenReturn(50.0);
        when(scoreCalculator.calculate(scenario, 2)).thenReturn(41.0);

        ScenarioComparison comparison = service.compareToCurrent(current, scenario, household);

        assertEquals(-9.0, comparison.getDifference());
        assertEquals(ScenarioComparison.Direction.WORSENED, comparison.getDirection());
    }

    // US-29: save a scenario

    @Test
    void savingAScenarioPersistsItWithItsNameAndBothScores() {
        when(scoreCalculator.calculate(current, 2)).thenReturn(50.0);
        when(scoreCalculator.calculate(scenario, 2)).thenReturn(62.5);
        SavedScenario stored = new SavedScenario(
                3, household.getId(), user.getId(), "Shorter showers", scenario, 50.0, 62.5, "now");
        when(scenarioDao.create(household.getId(), user.getId(), "Shorter showers", scenario, 50.0, 62.5))
                .thenReturn(stored);

        SavedScenario result = service.saveScenario(user, household, "Shorter showers", current, scenario);

        assertEquals("Shorter showers", result.getName());
        assertEquals(50.0, result.getBaselineScore());
        assertEquals(62.5, result.getProjectedScore());
    }

    @Test
    void savingAScenarioIgnoresSpacesAroundTheName() {
        when(scoreCalculator.calculate(current, 2)).thenReturn(50.0);
        when(scoreCalculator.calculate(scenario, 2)).thenReturn(62.5);

        service.saveScenario(user, household, "  Shorter showers  ", current, scenario);

        verify(scenarioDao, times(1))
                .create(household.getId(), user.getId(), "Shorter showers", scenario, 50.0, 62.5);
    }

    @Test
    void scenarioWithoutANameIsNotSaved() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveScenario(user, household, "   ", current, scenario));
        verifyNoInteractions(scenarioDao);
    }

    @Test
    void guestCannotSaveAScenario() {
        assertThrows(IllegalStateException.class,
                () -> service.saveScenario(null, household, "Shorter showers", current, scenario));
        verifyNoInteractions(scenarioDao);
    }

    // US-30: view saved scenarios

    @Test
    void savedScenariosAreListedForTheHousehold() {
        SavedScenario first = new SavedScenario(
                1, household.getId(), user.getId(), "Shorter showers", scenario, 50.0, 62.5, "now");
        SavedScenario second = new SavedScenario(
                2, household.getId(), 99L, "Add solar", scenario, 50.0, 70.0, "now");
        when(scenarioDao.findByHousehold(household.getId())).thenReturn(List.of(first, second));

        List<SavedScenario> result = service.findSavedScenarios(household);

        assertEquals(List.of(first, second), result);
    }

    // US-31: delete a saved scenario

    @Test
    void deletingAScenarioRemovesItFromItsHousehold() {
        service.deleteScenario(household, 3);

        verify(scenarioDao, times(1)).delete(3, household.getId());
    }
}
