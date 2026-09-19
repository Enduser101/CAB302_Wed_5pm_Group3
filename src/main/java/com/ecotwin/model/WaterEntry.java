package com.ecotwin.model;

public class WaterEntry {

    private final long id;
    private final long householdId;
    private final String period;
    private final double litres;
    private final String notes;
    private final Long updatedByUserId;
    private final String updatedAt;

    public WaterEntry(long id, long householdId, String period, double litres,
                       String notes, Long updatedByUserId, String updatedAt) {
        this.id = id;
        this.householdId = householdId;
        this.period = period;
        this.litres = litres;
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

    public double getLitres() {
        return litres;
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
