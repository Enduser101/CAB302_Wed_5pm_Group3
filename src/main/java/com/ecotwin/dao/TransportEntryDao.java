package com.ecotwin.dao;

import com.ecotwin.model.TransportEntry;

import java.util.List;

public interface TransportEntryDao {
    // US-18: one row per household per period - recording again for the same period updates it in place.
    TransportEntry upsertForPeriod(long householdId, String period, double publicTransportTripsPerWeek,
                                    double flightsPerYear, Long updatedByUserId);
    List<TransportEntry> findByHousehold(long householdId);
    TransportEntry update(long entryId,
                          double publicTransportTripsPerWeek,
                          double flightsPerYear,
                          long updatedByUserId);

}
