package com.ecotwin.dao;

import com.ecotwin.model.WaterEntry;

import java.util.List;

public interface WaterEntryDao {
    // US-16: append-only log, one row per recorded reading.
    WaterEntry create(long householdId, String period, double litres, String notes, Long updatedByUserId);
    List<WaterEntry> findByHousehold(long householdId);
}
