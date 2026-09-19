package com.ecotwin.model;

public class EnergyEntry {

    private final long id;
    private final long householdId;
    private final String period;
    private final double electricityKwh;
    private final Double solarGenerationKwh;
    private final String notes;
    private final Long updatedByUserId;
    private final String updatedAt;

    public EnergyEntry(long id, long householdId, String period, double electricityKwh, Double solarGenerationKwh,
                        String notes, Long updatedByUserId, String updatedAt) {
        this.id = id;
        this.householdId = householdId;
        this.period = period;
        this.electricityKwh = electricityKwh;
        this.solarGenerationKwh = solarGenerationKwh;
        this.notes = notes;
        this.updatedByUserId = updatedByUserId;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public long getHouseholdId() {
        return householdId;
    }

    public String getPeriod() {
        return period;
    }

    public double getElectricityKwh() {
        return electricityKwh;
    }

    public Double getSolarGenerationKwh() {
        return solarGenerationKwh;
    }

    public String getNotes() {
        return notes;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
