package com.ecotwin.dao;

import com.ecotwin.model.SavedScenario;
import com.ecotwin.model.SustainabilityScenario;

import java.util.List;

public interface ScenarioDao {
    // US-29: saved scenarios belong to the household, not to the member who saved them.
    SavedScenario create(long householdId, Long createdByUserId, String name,
                         SustainabilityScenario values, double baselineScore, double projectedScore);
    // US-30
    List<SavedScenario> findByHousehold(long householdId);
    // US-31: only removes the scenario if it belongs to the given household.
    void delete(long scenarioId, long householdId);
}
