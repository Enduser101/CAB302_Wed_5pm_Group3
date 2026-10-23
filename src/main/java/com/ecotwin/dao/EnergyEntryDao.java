package com.ecotwin.dao;

import com.ecotwin.model.EnergyEntry;

import java.util.List;

/**
 * Stores households' energy readings (DAO / Repository pattern).
 *
 * <p>{@link com.ecotwin.service.EnergyService} depends only on this interface, never on SQL.
 * The app uses {@link com.ecotwin.dao.sqlite.SqliteEnergyEntryDao}, and unit tests use a
 * Mockito mock, so the service's rules can be tested without a database.
 */
public interface EnergyEntryDao {

    /**
     * US-15: adds a new reading. Each call adds a row; earlier readings are kept.
     *
     * @param householdId        the household the reading belongs to
     * @param period             the month it covers, as {@code YYYY-MM}
     * @param electricityKwh     electricity used, in kWh
     * @param solarGenerationKwh solar generated in kWh, or {@code null} for none
     * @param notes              optional free-text notes
     * @param updatedByUserId    the user who recorded it, or {@code null} if unknown
     * @return the saved entry, with its new id
     */
    EnergyEntry create(long householdId, String period, double electricityKwh, Double solarGenerationKwh,
                        String notes, Long updatedByUserId);

    /**
     * @param householdId the household to look up
     * @return the household's readings, newest first
     */
    List<EnergyEntry> findByHousehold(long householdId);

    /**
     * US-19: overwrites an existing reading's values and records who changed it and when.
     *
     * @param entryId            the reading to change
     * @param electricityKwh     the new electricity use, in kWh
     * @param solarGenerationKwh the new solar generation in kWh, or {@code null} for none
     * @param notes              optional free-text notes
     * @param updatedByUserId    the user making the change
     * @return the reading as it is now stored
     * @throws IllegalArgumentException if no reading has that id
     */
    EnergyEntry update(long entryId, double electricityKwh, Double solarGenerationKwh, String notes, long updatedByUserId);
}
