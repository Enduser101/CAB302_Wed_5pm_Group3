package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.EnergyEntryDao;
import com.ecotwin.model.EnergyEntry;
import com.ecotwin.model.Household;
import com.ecotwin.model.User;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Monthly household energy readings: electricity used and, optionally, solar generated
 * (US-15, US-19, US-14).
 *
 * <p>Every change is written to the household's activity history and triggers a score
 * recalculation. Edits record the old and new values of only the fields that changed.
 */
public class EnergyService {

    private final EnergyEntryDao energyEntryDao;
    private final ActivityLogDao activityLogDao;
    private final ScoreService scoreService;

    /**
     * @param energyEntryDao where energy readings are stored
     * @param activityLogDao where history messages are written
     * @param scoreService   told to recalculate after every change
     */
    public EnergyService(EnergyEntryDao energyEntryDao, ActivityLogDao activityLogDao, ScoreService scoreService) {
        this.energyEntryDao = energyEntryDao;
        this.activityLogDao = activityLogDao;
        this.scoreService = scoreService;
    }

    /**
     * US-15: record the household's energy usage for the current period.
     *
     * @param actor              the user recording the reading
     * @param household          the household it belongs to
     * @param electricityKwh     electricity used this month, in kWh
     * @param solarGenerationKwh solar generated this month in kWh, or {@code null} if the
     *                           household has no solar
     * @param notes              optional free-text notes
     * @return the saved entry, dated to the current month ({@code YYYY-MM})
     * @throws IllegalArgumentException if either value is negative
     */
    public EnergyEntry recordEntry(User actor, Household household, double electricityKwh, Double solarGenerationKwh,
                                    String notes) {
        validateEnergyValues(electricityKwh, solarGenerationKwh);

        String period = YearMonth.now().toString();
        EnergyEntry entry = energyEntryDao.create(household.getId(), period, electricityKwh, solarGenerationKwh,
            notes, actor.getId());
        activityLogDao.log(household.getId(), actor.getId(), displayName(actor) + " recorded energy usage for " + period);
        scoreService.recalculate(household.getId());
        return entry;
    }

    /**
     * US-19: update an existing energy entry.
     *
     * <p>US-14: the history message names each value that changed with its old and new
     * value, e.g. "changed energy usage from 410.0 kWh to 395.0 kWh and solar generation
     * from none to 60.0 kWh". If nothing changed, it just says the entry was updated.
     *
     * @param actor              the user making the edit
     * @param household          the household the entry belongs to
     * @param entryId            the entry to change
     * @param electricityKwh     the corrected electricity use, in kWh
     * @param solarGenerationKwh the corrected solar generation in kWh, or {@code null} for none
     * @param notes              optional free-text notes
     * @return the updated entry
     * @throws IllegalArgumentException if either value is negative
     */
    public EnergyEntry updateEntry(User actor, Household household, long entryId,
                                   double electricityKwh, Double solarGenerationKwh, String notes) {
        validateEnergyValues(electricityKwh, solarGenerationKwh);
        EnergyEntry oldEntry = null;

        for (EnergyEntry entry : energyEntryDao.findByHousehold(household.getId())) {
            if (entry.getId() == entryId) {
                oldEntry = entry;
                break;
            }
        }

        EnergyEntry updatedEntry = energyEntryDao.update(
                entryId,
                electricityKwh,
                solarGenerationKwh,
                notes,
                actor.getId()
        );

        List<String> changes = new ArrayList<>();
        if (oldEntry != null && oldEntry.getElectricityKwh() != electricityKwh) {
            changes.add("energy usage from " + oldEntry.getElectricityKwh() + " kWh to " + electricityKwh + " kWh");
        }
        if (oldEntry != null && !Objects.equals(oldEntry.getSolarGenerationKwh(), solarGenerationKwh)) {
            changes.add("solar generation from " + solarText(oldEntry.getSolarGenerationKwh())
                    + " to " + solarText(solarGenerationKwh));
        }

        if (!changes.isEmpty()) {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " changed " + String.join(" and ", changes)
            );
        } else {
            activityLogDao.log(
                    household.getId(),
                    actor.getId(),
                    displayName(actor) + " updated energy usage for "
                            + updatedEntry.getPeriod()
            );
        }

        scoreService.recalculate(household.getId());

        return updatedEntry;
    }

    /**
     * @param household the household to look up
     * @return all of the household's energy readings
     */
    public List<EnergyEntry> findEntriesForHousehold(Household household) {
        return energyEntryDao.findByHousehold(household.getId());
    }

    /**
     * Rejects negative readings. Zero is allowed: it is a real reading, not a missing one.
     *
     * @param electricityKwh     electricity used, in kWh
     * @param solarGenerationKwh solar generated in kWh, or {@code null} for none
     * @throws IllegalArgumentException if either value is negative
     */
    private void validateEnergyValues(double electricityKwh, Double solarGenerationKwh) {
        if (electricityKwh < 0) {
            throw new IllegalArgumentException("Electricity usage cannot be negative");
        }

        if (solarGenerationKwh != null && solarGenerationKwh < 0) {
            throw new IllegalArgumentException("Solar generation cannot be negative");
        }
    }

    /**
     * @param kwh a solar reading, or {@code null}
     * @return the reading as shown in the history, e.g. "60.0 kWh", or "none" when there is no solar
     */
    private String solarText(Double kwh) {
        return kwh == null ? "none" : kwh + " kWh";
    }

    /**
     * @param user the user to name
     * @return their display name, or their username if they have not set one
     */
    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}