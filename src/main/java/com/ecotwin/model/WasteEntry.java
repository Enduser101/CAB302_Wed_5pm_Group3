package com.ecotwin.model;

public class WasteEntry {

    private final long id;
    private final long householdId;
    private final String period;
    private final double generalKg;
    private final double recycledKg;
    private final double compostKg;
    private final Long updatedByUserId;
    private final String updatedAt;

    public WasteEntry(long id, long householdId, String period, double generalKg, double recycledKg, double compostKg,
                       Long updatedByUserId, String updatedAt) {
        this.id = id;
        this.householdId = householdId;
        this.period = period;
        this.generalKg = generalKg;
        this.recycledKg = recycledKg;
        this.compostKg = compostKg;
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

    public double getGeneralKg() {
        return generalKg;
    }

    public double getRecycledKg() {
        return recycledKg;
    }

    public double getCompostKg() {
        return compostKg;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
