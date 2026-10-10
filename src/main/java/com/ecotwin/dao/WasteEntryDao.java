package com.ecotwin.dao;

import com.ecotwin.model.WasteEntry;

import java.util.List;

public interface WasteEntryDao {
    // US-17: append-only log, one row per recorded reading.
    WasteEntry create(long householdId, String period, double generalKg, double recycledKg, double compostKg,
                       Long updatedByUserId);
    List<WasteEntry> findByHousehold(long householdId);
    WasteEntry update(long entryId,
                      double generalWasteKg,
                      double recyclingKg,
                      double organicWasteKg,
                      long updatedByUserId);
}
