package com.ecotwin.model;

public class Vehicle {

    private final long id;
    private final long householdId;
    private final String label;
    private final String fuelType;
    private final double kmPerWeek;

    public Vehicle(long id, long householdId, String label, String fuelType, double kmPerWeek) {
        this.id = id;
        this.householdId = householdId;
        this.label = label;
        this.fuelType = fuelType;
        this.kmPerWeek = kmPerWeek;
    }

    public long getId() {
        return id;
    }

    public long getHouseholdId() {
        return householdId;
    }

    public String getLabel() {
        return label;
    }

    public String getFuelType() {
        return fuelType;
    }

    public double getKmPerWeek() {
        return kmPerWeek;
    }
}
