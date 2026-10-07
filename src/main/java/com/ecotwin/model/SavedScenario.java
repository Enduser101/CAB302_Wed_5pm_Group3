package com.ecotwin.model;

/**
 * US-29: a named scenario saved for the whole household, with the scores it had when saved.
 */
public class SavedScenario {

    private final long id;
    private final long householdId;
    private final Long createdByUserId;
    private final String name;
    private final SustainabilityScenario values;
    private final double baselineScore;
    private final double projectedScore;
    private final String createdAt;

    public SavedScenario(long id, long householdId, Long createdByUserId, String name,
                         SustainabilityScenario values, double baselineScore,
                         double projectedScore, String createdAt) {
        this.id = id;
        this.householdId = householdId;
        this.createdByUserId = createdByUserId;
        this.name = name;
        this.values = values;
        this.baselineScore = baselineScore;
        this.projectedScore = projectedScore;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getHouseholdId() {
        return householdId;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public String getName() {
        return name;
    }

    public SustainabilityScenario getValues() {
        return values;
    }

    /** The household's current score at the time the scenario was saved. */
    public double getBaselineScore() {
        return baselineScore;
    }

    public double getProjectedScore() {
        return projectedScore;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
