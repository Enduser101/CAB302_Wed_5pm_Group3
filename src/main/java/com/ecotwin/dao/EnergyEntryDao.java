package com.ecotwin.dao;

import com.ecotwin.model.EnergyEntry;

import java.util.List;

public interface EnergyEntryDao {
    // US-15: append-only log, one row per recorded reading.
    EnergyEntry create(long householdId, String period, double electricityKwh, Double solarGenerationKwh,
                        String notes, Long updatedByUserId);
    List<EnergyEntry> findByHousehold(long householdId);

    EnergyEntry update(long entryId, double electricityKwh, Double solarGenerationKwh, String notes, long updatedByUserId);
}
