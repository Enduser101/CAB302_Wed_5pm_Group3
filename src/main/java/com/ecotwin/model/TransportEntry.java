package com.ecotwin.model;

public class TransportEntry {

    private final long id;
    private final long householdId;
    private final String period;
    private final double publicTransportTripsPerWeek;
    private final double flightsPerYear;
    private final Long updatedByUserId;
    private final String updatedAt;

    public TransportEntry(long id, long householdId, String period, double publicTransportTripsPerWeek,
                           double flightsPerYear, Long updatedByUserId, String updatedAt) {
        this.id = id;
        this.householdId = householdId;
        this.period = period;
        this.publicTransportTripsPerWeek = publicTransportTripsPerWeek;
        this.flightsPerYear = flightsPerYear;
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

    public double getPublicTransportTripsPerWeek() {
        return publicTransportTripsPerWeek;
    }

    public double getFlightsPerYear() {
        return flightsPerYear;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
